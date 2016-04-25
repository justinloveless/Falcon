package com.example.justin.falcontest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.AsyncTask;
import android.widget.Toast;

//import com.google.android.exoplayer.ExoPlaybackException;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.vrtoolkit.cardboard.CardboardView;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SocketClient extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener{

    SharedPreferences settings_prefs;
    String IpAddress;
    String port;
    TextView textResponse;
    TextView sensorVals;
    WebView mWebView;
    Button buttonClose;
    Socket socket = null;
    SocketAddress sockAddr;
    private SensorManager mSensorManager;
    private Sensor mAcc, mMag;
    private boolean stop;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 5000; //5 seconds
    private BluetoothLeScanner mLEScanner;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    public List<BluetoothGattService> mServices;
    public BluetoothManager bluetoothManager;
    public ScanSettings settings;
    public String devName;
    public ArrayList<String> sArray = new ArrayList<String>();
    public ArrayAdapter ServiceAdapter;
    public ListView serviceList;
    Timer MyClientTaskTimer;
    boolean toastMade;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonClose = (Button)findViewById(R.id.closeSocket);
        textResponse = (TextView)findViewById(R.id.responseSocket);
        sensorVals = (TextView)findViewById(R.id.sensorData);
        mWebView = (WebView)findViewById(R.id.piviewer);

        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
//        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        port = settings_prefs.getString("Port", "8888");
//        final SharedPreferences.Editor edit_port = settings_prefs.edit();

        //Enable Javascript
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);//force links and redirects to open in web view instead of browser
        mWebView.setWebViewClient(new WebViewClient());
        Log.d("IP Address", "IP Adress = " + IpAddress);
        mWebView.loadUrl( "http://" + IpAddress + "/piviewer");


        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
                if (!stop) {
                    cleanup();
                    buttonClose.setText("Restart");
                }else {
                    startup();
                    buttonClose.setText("Stop");
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            Log.i("Sensors", "There is a gyroscope");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Log.i("Sensors", "There is an accelerometer");
        }





        //***********Bluetooth stuff**************//
        //check if BLE is supported
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.d("BLE", "BLE supported");

        /*final BluetoothManager */bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //enable Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        Log.d("BLE", "Done with BLE init");

        // turn on location services if they aren't already (forcefully, muahaha)
        //  (BLE scan won't work without GPS on)
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);

//        MyClientTaskRepeated();
        MyClientTaskTimer = new Timer();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //happens immediately after onCreate() or onRestart();

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            Log.d("onResume", "About to scan for devices");
            scanLeDevice(true);
        }
        startup();
    }

    @Override
    protected void onPause() {
        cleanup();
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        cleanup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startup();
    }

    @Override
    protected void onDestroy(){
//        cleanup();
        super.onDestroy();
        try {
            Log.d("onDestroy", "Got past super.onDestroy()");
            if (mGatt == null) {
                Log.d("onDestroy", "mGatt is null");
                return;
            }
            Log.d("onDestroy", "mGatt is not null");
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
        catch (Exception e) {
            Log.e("onDestroy", e.getMessage().toString());
        }
    }

    public String getDataToSend(){
        return sensorData;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        Log.d("scanLeDevice", "In Function");
        if (enable) {
            //turn scan off after a predetermined time
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                    Log.d("scanLeDevice", "Scan stopped");
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.d("BLE Scan", "about to start scan");
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    //return scan results back to the app
    private ScanCallback mScanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            if (result.getScanRecord().getDeviceName() != null) {
                devName = result.getScanRecord().getDeviceName().toString();
                if (!existsInArray(sArray, devName)){//only unique values
                    //add new string to array
                    sArray.add(devName);
                }
            }
            else {
                devName = "null";
            }

            Log.i("device Name", devName);

            //connect to CC2650 SensorTag
            if (devName.matches("CC2650 SensorTag")) {
                Log.d("connecting", "Connecting to device: "+ devName);
                connectToDevice(result.getDevice());
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        Log.d("connectToDevice", "Connecting to device " + device.getName());
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
            try {
//                deviceName.setText(device.getName());
//                deviceName.setText(devName);
            } catch (Exception e) {
                Log.e("ConnectToDevice", "Unable to set name. Device: " + device.toString()
                        + "\nName: " + device.getName()
                        + "\nError: " + e.getMessage() + " at line " + e.getStackTrace()[2].getLineNumber());
                e.printStackTrace();
            }
        }
        else {
            Log.e("connecToDevice", "mGatt not null: " + mGatt.toString());
            scanLeDevice(false);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        private int mReadWriteState = 0;
        private void advanceRW(){mReadWriteState++;}
        private void resetRWState() {mReadWriteState = 0;}
        public int readState = 0;
        public double g_x=0;
        public double g_y=0;
        public double g_z=0;
        public double a_x =0;
        public double a_y=0;
        public double a_z=0;
        public double m_x = 0;
        public double m_y = 0;
        public double m_z = 0;
        public boolean connParamWritten = false;
        public long prevNot, curNot;
        float[] inR = new float[16];
        float[] outR = new float[16];
        float[] I = new float[16];
        float[] gravity = new float[3];
        float[] geomag = new float[3];
        float[] orientVals = new float[3];
        double Roll, Pitch;


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
            switch (status){
                case 8:
                    Log.e("gattCallback", "TIMEOUT");
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

            byte[] val = characteristic.getValue();
            g_x = ((val[1] << 8) + val[0]) / 128.0;
            g_y = ((val[3] << 8) + val[2]) / 128.0;
            g_z = ((val[5] << 8) + val[4]) / 128.0;
            a_x = (((val[7] << 8) + val[6]) / (32768.0 / 8.0)) * -1;
            a_y = ((val[9] << 8) + val[8]) / (32768.0 / 8.0);
            a_z = (((val[11] << 8) + val[10]) / (32768.0 / 8.0)) * -1;
            m_x = 1.0 * ((val[13] << 8) + val[12])/* / (32768 / 4912)*/;
            m_y = 1.0 * ((val[15] << 8) + val[14]) /*/ (32768 / 4912)*/;
            m_z = 1.0 * ((val[17] << 8) + val[16])/* / (32768 / 4912)*/;
//            Log.i("Notice2", String.format("\tGyr: \t%.2f\t%.2f\t%.2f\tAcc: \t%.2f\t%.2f\t%.2f\tMag: \t%.2f\t%.2f\t%.2f"
//                    , g_x, g_y, g_z
//                    , a_x, a_y, a_z
//                    , m_x, m_y, m_z));
            Roll = Roll - Roll/5.0 + (Math.atan2(a_y, a_z)*180/Math.PI)/5.0;
            Pitch = Pitch - Pitch/5.0 + (Math.atan2(a_x, Math.sqrt(a_y*a_y + a_z*a_z)) *180/Math.PI)/5.0;
            Log.d("BLE vals", String.format("Pitch:\t%.2f\tRoll:\t%.2f" , Pitch, Roll));

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            mServices = services;
            Log.i("onServicesDiscovered", services.toString());
            Log.i("onServicesDiscovered", services.get(0).toString());
            //add services to array
            sArray.add(services.toString());
            services.get(0).getIncludedServices();
            for (int i =0; i < services.size(); i++) {
                UUID ser = services.get(i).getUuid();
                Log.i("characteristicDiscovery", "[" + i + "]" + ser.toString());
                for (int j = 0; j < services.get(i).getCharacteristics().size(); j++) {
                    final BluetoothGattCharacteristic c = services.get(i).getCharacteristics().get(j);
                    UUID chara_s = c.getUuid();
                    Log.i("characteristicDiscovery", "[" + i + "][" + j + "] " + chara_s.toString()
                            + " " + SensorTagGatt.lookup(chara_s.toString(), "notFound"));

                    for (int k = 0; k < services.get(i).getCharacteristics().get(j).getDescriptors().size(); k++) {
                        final BluetoothGattDescriptor d = services.get(i).getCharacteristics().get(j).getDescriptors().get(k);
                        UUID desc = d.getUuid();
                        Log.i("DescriptorDiscovery", "[" + i + "][" + j + "][" + k + "]" + desc.toString());
                        if (desc.toString().contains("2902")) {
                            Log.i("ConfigDescriptor", " Conf for: " + SensorTagGatt.lookup(chara_s.toString(), "notFound"));
                        }

                    }
                }
            }
//            BluetoothGattService s = services.get(6);
//            BluetoothGattCharacteristic c = s.getCharacteristics().get(1);
//            byte[] val = new byte[1];
//            val[0] = 0x7f;
//            c.setValue(val);
//            Log.d("writing", "" + gatt.writeCharacteristic(c));
            resetRWState();
            SensorTagCommunication();
        }


        public void SensorTagCommunication(){
            String TAG = "SensorTagComms";
            BluetoothGattService mSvc = mServices.get(6);
//            Log.d(TAG, "BT Gatt Svc = " + mSvc.toString());
            Log.d(TAG, "state: " + mReadWriteState);
            BluetoothGattCharacteristic mCh;
            BluetoothGattDescriptor mDes;
            switch(mReadWriteState){
                case 0: //write motion configuration
                    mSvc = mServices.get(6);
                    mCh = mSvc.getCharacteristic(
                            UUID.fromString("f000aa82-0451-4000-b000-000000000000"));
                    Log.d(TAG, "BT Gatt Char = " + mCh.toString());
                    mCh.setValue(new byte[]{0x7f, 0b00000100});
                    Log.d(TAG, "" + mGatt.writeCharacteristic(mCh));
                    break;
                case 1: //write Period
                    mSvc = mServices.get(6);
                    mCh = mSvc.getCharacteristic(UUID.fromString("f000aa83-0451-4000-b000-000000000000"));
                    mCh.setValue(new byte[]{0x0a});
                    Log.d(TAG, "" + mGatt.writeCharacteristic(mCh));
                    break;
                case 2: //write motion notification descriptor
//                    mSvc = mServices.get(6);
//                    mCh = mSvc.getCharacteristic(
//                            UUID.fromString("f000aa81-0451-4000-b000-000000000000"));
//                    mDes = mCh.getDescriptors().get(0);
//                    mDes.setValue(new byte[] {0x01, 0x00}); //enable remotely
//                    mGatt.setCharacteristicNotification(mCh, true); // enable locally
//                    Log.d(TAG, "" + mGatt.writeDescriptor(mDes));
//                    prevNot = System.currentTimeMillis();
                    advanceRW();
                    SensorTagCommunication();
                    break;
//                case 101: //set up notifications on ccc1
//                    mSvc = mServices.get(11);
//                    mCh = mSvc.getCharacteristic(UUID.fromString("f000ccc1-0451-4000-b000-000000000000"));
//                    mDes = mCh.getDescriptors().get(0);
//                    mDes.setValue(new byte[] {0x01, 0x00}); //enable remotely
//                    mGatt.setCharacteristicNotification(mCh, true); // enable locally
//                    Log.d(TAG, "" + mGatt.writeDescriptor(mDes));
//                    break;
//                case 100: // write connection parameters
//                    mSvc = mServices.get(11);
//                    mCh = mSvc.getCharacteristic(UUID.fromString("f000ccc2-0451-4000-b000-000000000000"));
//                    //TODO figure out connection parameters to get connection to be more steady
//                    byte[] maxConInt = new byte[] {40,0};
//                    byte[] minConInt = new byte[] {40,0};
//                    byte[] slvLat = new byte[] {0,0};
//                    byte[] supTO = new byte[] {0x7f,0x06};
////                    byte[] tot = new byte[] {   supTO[1], supTO[0],slvLat[1], slvLat[0], minConInt[1],
////                            minConInt[0],maxConInt[1], maxConInt[0]};
//                    byte[] tot = new byte[] {maxConInt[0], maxConInt[1], minConInt[0],
//                            minConInt[1], slvLat[0], slvLat[1], supTO[0], supTO[1]};
//                    mCh.setValue(tot);
//                    Log.d(TAG, "" + mGatt.writeCharacteristic(mCh));
//                    break;
//                case 102: // wait for notification from ccc1
//                    //TODO wait until notification says that connection parameters have been written
//                    if (connParamWritten){
//                        advanceRW();
//                    }
//                    SensorTagCommunication();
//                    break;
//                case 105: //read connection parameters
//                    mSvc = mServices.get(11);
//                    mCh = mSvc.getCharacteristic(UUID.fromString("f000ccc1-0451-4000-b000-000000000000"));
//                    Log.d(TAG, "" + mGatt.readCharacteristic(mCh));
//                    advanceRW();
//                    break;
                case 3: //read data
                    mSvc = mServices.get(6);
                    mCh = mSvc.getCharacteristic(UUID.fromString("f000aa81-0451-4000-b000-000000000000"));
                    Log.d(TAG, "" + mGatt.readCharacteristic(mCh));
                    advanceRW();
                    break;
                case 4: //empty state
//                    MyClientTaskRepeated();
                    MyClientTaskTimer.scheduleAtFixedRate(MyClientTaskTimertask, 0, 100);
                    break;
                default:
                    resetRWState();//go to initial state
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.d("CharRead", "reading Characteristic");
            if (characteristic.getUuid().toString().equals("f000aa81-0451-4000-b000-000000000000") ) {
                byte[] val = characteristic.getValue();
                char[] val_s = characteristic.getValue().toString().toCharArray();
//                g_x = ((val[1] << 8) + val[0]) / 128.0;
//                g_y = ((val[3] << 8) + val[2]) / 128.0;
//                g_z = ((val[5] << 8) + val[4]) / 128.0;
                a_x = (((val[7] << 8) + val[6]) / (32768.0 / 2.0)) * -1;
                a_y = ((val[9] << 8) + val[8]) / (32768.0 / 2.0);
                a_z = (((val[11] << 8) + val[10]) / (32768.0 / 2.0)) * -1;
                //magnetometer conversion is done on chip, so no calculation should be needed
//                m_x = 1.0 * ((val[13] << 8) + val[12])/* / (32768 / 4912)*/;
//                m_y = 1.0 * ((val[15] << 8) + val[14]) /*/ (32768 / 4912)*/;
//                m_z = 1.0 * ((val[17] << 8) + val[16])/* / (32768 / 4912)*/;
//                Log.i("value", "\tGyr: \t" + g_x + "\t,\t" + g_y + "\t,\t" + g_z
//                        + "\tAcc: \t" + a_x + "\t,\t" + a_y + "\t,\t" + a_z
//                        + "\tMag: \t" + m_x + "\t,\t" + m_y + "\t,\t" + m_z);
                //calculate roll
                Roll = (Math.atan2(a_y, a_z)*180/Math.PI);
                //calculate pitch
                Pitch = (Math.atan2(a_x, Math.sqrt(a_y*a_y + a_z*a_z)) *180/Math.PI);
//                Log.d("BLE vals", String.format("Pitch:\t%.2f\tRoll:\t%.2f" , Pitch, Roll));


                //map Pitch to pitch command values
                Pitch = dPitchMin + ((dPitchMax - dPitchMin)/(pitchMax - pitchMin))*(Pitch - pitchMin);
                pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + Pitch/avgRange; // calculate exponential average
//                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
                sPitch = String.format("%.0f", pitchAvg);


                if ( Roll > 10 || Roll < -10){// roll control
                    if (Roll > rollMax)
                        Roll = rollMax;
                    else if (Roll < rollMin)
                        Roll = rollMin;
//                    Log.d("sensors", "outter");
                    Roll = dRollMin + ((dRollMax - dRollMin)/(rollMax - rollMin))*(Roll - rollMin);
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + Roll/avgRange; // calculate exponential average
//                    roll.setText("roll = " + String.format("%.0f", mRoll));
                    sRoll = String.format("%.0f", rollAvg);
                }else {
//                    Log.d("sensors", "inner");
                    Roll = (dRollMax - dRollMin)/2 + dRollMin;
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + Roll/avgRange; // calculate exponential average
//                    roll.setText("roll = " + String.format("%.0f", mRoll));
                    sRoll = String.format("%.0f", rollAvg);
                }


                sensorData = (sRoll + "," + sPitch + "," + sThrottle + "," + sYaw);
//                sensorVals.setText(sensorData);
//                Log.d("sensors2", sensorData);
                setSensorData(sensorData);
            }
            else if (characteristic.getUuid().toString().equals("f000ccc1-0451-4000-b000-000000000000") ){
                byte[] val = characteristic.getValue();
                double cnctInt = 1.0*((val[1] << 8) + val[0]);
                double slvLat = 1.0*((val[3] << 8) + val[2]);
                double supTO = 1.0*((val[5] << 8) + val[4]);
                Log.d("connectionParams","Connection Interval: " + cnctInt + " Slave Latency: "
                        + slvLat + " Supervisor Timeout" + supTO);
            }
            //TODO uncomment this line to repeatedly read Movement Sensor
            mGatt.readCharacteristic(characteristic);
//            SensorTagCommunication();
        }




        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("onDescriptorWrite", "descriptor: " + descriptor + " written to");
            advanceRW();
            SensorTagCommunication();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d("onReadRemoteRssi", "Remote RSSI: " + rssi);
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] val = characteristic.getValue();
            int i = val[0];
            String sent = "";
            for (int j = 0; j < val.length; j++){
                sent += "[" + val[j] + "] ";
            }
//            Log.i("onCharacteristicWrite", "i=" + i);
            Log.i("onCharacteristicWrite", "status: " + status + " bytes written:" + val.length +
                    " {"+sent +"} to " + characteristic.getUuid().toString());
            advanceRW();
            SensorTagCommunication();
        }
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i("onReliableWrite", " well, that just happened... ");
        }
    };

    public boolean existsInArray (ArrayList<String> array, String s) {
        for (String str : array){
            if (s.equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("RequestPermisResult", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }




    float[] inR = new float[16];
    float[] outR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    private static final double dPitchMin = 1300;//drone pitch
    private static final double dPitchMax = 1600;
    private static final double dThrottleMin = 1250; //drone throttle
    private static final double dThrottleMax = 1900;
    private static final double dThrottleNom = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
    private static final double pitchMin = -30; // pitch from phone controls drone throttle
    private static final double pitchMax = 30;
    private static final double dRollMin = 1400; //drone roll
    private static final double dRollMax = 1700;
    private static final double rollMin = -90;
    private static final double rollMax = 90;
    private static final double dYawMin = 1400;
    private static final double dYawMax = 1700;
    private static final double yawMin = -180;
    private static final double yawMax = 180;
    private static final double avgRange = 15;
    private double pitchAvg, rollAvg, throttleAvg, yawAvg;
    private double yawInit;
    String sPitch, sRoll, sThrottle, sYaw, sensorData;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gravity == null || geomag == null){
            throttleAvg = dThrottleNom;
            pitchAvg = (dPitchMax - dPitchMin)/2.0 + dPitchMin;
            rollAvg = (dRollMax - dRollMin)/2.0 + dRollMin;
            yawAvg = (dYawMax - dYawMin)/2.0 + dYawMin;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            Log.i("Acc", String.format("%.2f\t%.2f\t%.2f", event.values[0], event.values[1], event.values[2]));
            gravity = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
//            Log.i("Gyr", String.format("%.2f\t%.2f\t%.2f", event.values[0], event.values[1], event.values[2]));
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//            Log.i("Mag", String.format("%.2f\t%.2f\t%.2f", event.values[0], event.values[1], event.values[2]));
            geomag = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
//            Log.i("Rot", String.format("%.2f\t%.2f\t%.2f", event.values[0],
//                    event.values[1],event.values[2]));
        }
        if (gravity != null && geomag != null){ //wait until the first time the sensors are gathered
            boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);
            if (success) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientVals);
                double mAzimuth = Math.toDegrees(orientVals[0]);
                double mPitch = Math.toDegrees(orientVals[1])*-1.0;
                double mRoll = Math.toDegrees(orientVals[2])*-1.0;
//                pitch.setText("pitch = " + String.format("%.3f", mPitch));
//                roll.setText(String.format("%.3f",  mRoll));
//                sRoll = String.format("%.3f",  mRoll);
//                yaw.setText(String.format("%.3f", mAzimuth));
                sYaw = String.format("%.3f", mAzimuth);
//                Log.i("orient", String.format("%.2f\t%.2f\t%.2f", mAzimuth, mPitch, mRoll));
//                if (mPitch > 15 || mPitch < -15){//throttle control
                    if (mPitch > pitchMax)
                        mPitch = pitchMax;
                    else if (mPitch < pitchMin)
                        mPitch = pitchMin;
                    if (mPitch > 10){ // create window of control for throttle
                        mPitch = dThrottleNom
                                + ((dThrottleMax - dThrottleNom)/(pitchMax  - 15))*(mPitch - 15);
                        throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
                    }else if (mPitch < -10){
                        mPitch = dThrottleMin
                                + ((dThrottleNom - dThrottleMin)/(-15  - pitchMin))*(mPitch - pitchMin);
                        throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
                    } else { // handle "throttle latch zone"
                        mPitch = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
                        throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
                    }
//                    mPitch = dThrottleMin + ((dThrottleMax - dThrottleMin)/(pitchMax  - pitchMin))*(mPitch - pitchMin);
//                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
                    sThrottle = String.format("%.0f", throttleAvg);
//                    mPitch = (dPitchMax - dPitchMin)/2 + dPitchMin;
//                    pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
////                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
//                    sPitch = String.format("%.0f", pitchAvg);
//                }
//                else { //pitch control
////                    mPitch = dPitchMin + ((dPitchMax - dPitchMin)/(pitchMax - pitchMin))*(mPitch - pitchMin);
////                    pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
//////                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
////                    sPitch = String.format("%.0f", pitchAvg);
//                    mPitch = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
//                    throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
////                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
//                    sThrottle = String.format("%.0f", throttleAvg);
//                }
//                Log.d("sensors", "roll = " + mRoll);
//                if ( mRoll > 100 || mRoll < 80){// roll control
//                    if (mRoll > rollMax)
//                        mRoll = rollMax;
//                    else if (mRoll < rollMin)
//                        mRoll = rollMin;
////                    Log.d("sensors", "outter");
//                    mRoll = dRollMin + ((dRollMax - dRollMin)/(rollMax - rollMin))*(mRoll - rollMin);
//                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + mRoll/avgRange; // calculate exponential average
////                    roll.setText("roll = " + String.format("%.0f", mRoll));
//                    sRoll = String.format("%.0f", rollAvg);
//                }else {
////                    Log.d("sensors", "inner");
//                    mRoll = (dRollMax - dRollMin)/2 + dRollMin;
//                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + mRoll/avgRange; // calculate exponential average
////                    roll.setText("roll = " + String.format("%.0f", mRoll));
//                    sRoll = String.format("%.0f", rollAvg);
//                }
                //yaw control
                //TODO implement a BANG-BANG yaw control
                mAzimuth = dYawMin + ((dYawMax - dYawMin)/(yawMax - yawMin))*(mAzimuth - yawMin);
//                yaw.setText("yaw = " + String.format("%.0f", mAzimuth));
                yawAvg = yawAvg*(1.0 - 1.0/avgRange) + mAzimuth/avgRange; // calculate exponential average
//                yawAvg = ((dYawMax - dYawMin)/2+dYawMin)*(1.0 - 1.0/avgRange) + mAzimuth/avgRange; // calculate exponential average
                sYaw = String.format("%.0f", yawAvg);
                sensorData = (sRoll + "," + sPitch + "," + sThrottle + "," + sYaw);
//                sensorVals.setText(sensorData);
//                Log.d("sensors1", sensorData);
                setSensorData(sensorData);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setSensorData(final String data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView)findViewById(R.id.sensorData);
                t.setText(data);
            }
        });
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String dataToSend;
        Socket s;

        MyClientTask(String addr, int port, String msg, Socket sock) {
            dstAddress = addr;
            dstPort = port;
            dataToSend = msg;
            s = sock;

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                // Socket Code!!!
                //loop through this
                dataToSend = getDataToSend();
                if (socket == null || socket.isClosed()) {
                    Log.d("myClientTask", "Socket is null or closed");
                    try {
                        socket = new Socket(dstAddress, dstPort); //make new socket if first pass
                        if (socket == null || socket.isClosed())
                            Log.d("myClientTask", "new socket failed to be created");
                    } catch (ConnectException e){
//                        e.printStackTrace();
                        if (!toastMade) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Cool your jets!\nFalcon is not ready", Toast.LENGTH_SHORT).show();
                                }
                            });
                            toastMade = true;
                        }
//                        MyClientTaskTimer.cancel();
//                        this.cancel(false);

                    }
                }
                else if (!socket.isConnected()){
                    socket.connect(sockAddr); // reconnect if connection is lost
                    Log.d("myClientTask", "Socket is not connected");
                } else {
                    Log.d("myClientTask", "Socket=" + (socket == null ? "null" : ("" + socket.toString())));
                    if (dataToSend != null && s != null) {
                        Log.d("sending", "About to send data");
                        if (dataToSend != "close") {
                            // add in a beginning of transmission symbol. The "=" symbol is arbitrary, but must
                            // be the same on the raspberry pi
                            dataToSend = "=" + dataToSend + "\n";
                            Log.d("dataSent", dataToSend);
                            OutputStream toServer = socket.getOutputStream();
                            DataOutputStream out = new DataOutputStream(toServer);
//                            try {
                                out.writeUTF(dataToSend + "\n");
//                            } catch(IOException e){
//                                Log.e("socket", "Write was not able to complete");
//                            }
                        } else {
                            socket.close();
//                        if (dataToSend == "close") {
//                            socket.close();
//                        }
                        }
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            cleanup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.socket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_webview) {
            // Handle the webview activity
//            cleanup();
//            Intent intent = new Intent(SocketClient.this, Picam.class);
//            String IpAddr = "@string/IPAddress";
//            intent.putExtra("IP", IpAddr);
//            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_bluetooth) {
            // Handle the web socket activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, BluetoothLE.class);
            startActivity(intent);

        } else if (id == R.id.nav_home) {
            cleanup();
            Intent intent = new Intent(SocketClient.this, NavActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private TimerTask  MyClientTaskTimertask = new TimerTask() {
        @Override
        public void run(){
            try {
                MyClientTask myClientTask = new MyClientTask(IpAddress,
                        Integer.parseInt(port),
                        getDataToSend(),
                        socket);
                if (myClientTask.getStatus() == AsyncTask.Status.RUNNING) {
                    myClientTask.cancel(true);
                } else {
                    myClientTask.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    };

    private void MyClientTaskRepeated() {
        Timer timer = new Timer();
        if (stop == true){
            timer.cancel();
        } else {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (stop == true) {
//                        mWebView.loadUrl("https://s-media-cache-ak0.pinimg.com/736x/a2/57/90/a2579035dbf671b787a302aa816477a3.jpg");
                        this.cancel();
                    } else {
                        try {
                            MyClientTask myClientTask = new MyClientTask(IpAddress,
                                    Integer.parseInt(port),
                                    getDataToSend(),
                                    socket);
                            if (myClientTask.getStatus() == AsyncTask.Status.RUNNING) {
                                myClientTask.cancel(true);
                            } else {
                                myClientTask.execute();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 100);
        }
    }

//    public class LoadImage extends AsyncTask <String, Void, Bitmap>{
//        @Override
//        protected Bitmap doInBackground(String ... params) {
//            try {
//                InputStream is = (InputStream) new URL(params[0]).getContent();
////                Drawable d = Drawable.createFromStream(is, null);
//                Bitmap orig = BitmapFactory.decodeStream(is);
////                Bitmap targetL = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Canvas canvas = new Canvas(targetL);
////                RectF rectF = new RectF(0,0,1200,1080);
////                Path path = new Path();
////                path.addRect(rectF, Path.Direction.CW);
////                canvas.clipPath(path);
////                canvas.drawBitmap(orig, new Rect(0, 0, orig.getWidth(), orig.getHeight()),
////                        new Rect(0, 0, targetL.getWidth(), targetL.getHeight()), new Paint());
////                Matrix m = new Matrix();
////                m.postScale(1f, 1f);
////                Bitmap resized = Bitmap.createBitmap(targetL, 0,0,1500,1080, m, true);
////                BitmapDrawable bd = new BitmapDrawable(orig);
//                return orig;
//            }catch (Exception e) {
//                return null;
//            }
//        }
//        @Override
//        protected void onPostExecute(Bitmap d){
//            if (d != null){
////                Bitmap targetL = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Bitmap targetR = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Canvas canvasL = new Canvas(targetL);
////                Canvas canvasR = new Canvas(targetR);
////                RectF rectFL = new RectF(0, 0, 1200, 1080);
////                RectF rectFR = new RectF(720, 0, 1920, 1080);
////                Path pathL = new Path();
////                Path pathR = new Path();
////                pathL.addRect(rectFL, Path.Direction.CW);
////                pathR.addRect(rectFR, Path.Direction.CW);
////                canvasL.clipPath(pathL);
////                canvasR.clipPath(pathR);
////                canvasL.drawBitmap(d, new Rect(0, 0, d.getWidth(), d.getHeight()),
////                        new Rect(0, 0, targetL.getWidth(), targetL.getHeight()), new Paint());
////                canvasR.drawBitmap(d, new Rect(0, 0, d.getWidth(), d.getHeight()),
////                        new Rect(0, 0, targetR.getWidth(), targetR.getHeight()), new Paint());
////                Matrix m = new Matrix();
////                m.postScale(1f,1f);
////                Bitmap resizedL = Bitmap.createBitmap(targetL, 0,0,1920,1080, m, true);
////                Bitmap resizedR = Bitmap.createBitmap(targetR, 0,0,1920,1080, m, true);
////                BitmapDrawable bdL = new BitmapDrawable(resizedL);
////                BitmapDrawable bdR = new BitmapDrawable(resizedR);
////                picam1.setImageDrawable(bdL);
////                picam2.setImageDrawable(bdR);
//                BitmapDrawable bd = new BitmapDrawable(d);
//                picam1.setImageDrawable(bd);
//                picam2.setImageDrawable(bd);
//            } else {
//                Toast.makeText(SocketClient.this, "Image doesn't exist, or network error", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void LoadImageRepeatedly() {
//        Timer t = new Timer();
//        if (stop == true){
//            t.cancel();
//        } else {
//            t.scheduleAtFixedRate(new TimerTask() {
//                //            String x = "", y = "";
////            int i = 0, j = 0;
//                @Override
//                public void run() {
//                    if (stop == true){
//                        new LoadImage()
//                                .execute("https://s-media-cache-ak0.pinimg.com/736x/a2/57/90/a2579035dbf671b787a302aa816477a3.jpg");
//                        this.cancel();
//                    } else {
////                        x = "" + (300 + i++%7);
////                        y = "" + (200 + j++%13);
////
////                        new LoadImage().execute("https://unsplash.it/"+x+"/"+y);
////                        Log.d("image", "http://" + IpAddress + "/picam/cam.jpg");
//                        try {
//                            new LoadImage().execute("http://" + IpAddress + "/picam/cam.jpg");
//                            MyClientTask myClientTask = new MyClientTask(IpAddress,
//                                    Integer.parseInt(port),
//                                    getDataToSend(),
//                                    socket
//                            );
//                            if (myClientTask.getStatus() == AsyncTask.Status.RUNNING) {
//                                myClientTask.cancel(true);
//                                Log.d("Timer", "myClientTask is Running, has been cancelled");
//                            } else {
//                                myClientTask.execute();
//                                Log.d("Timer", "myClientTask has been executed");
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
////                        if (myClientTask.getStatus() == AsyncTask.Status.RUNNING){
////                            myClientTask.cancel(true);
////                        }
////                        else {
////                            myClientTask.execute();
////                        }
//                    }
//                }
//            }, 0, 105);
//        }
//    }

    private boolean cleanup (){
        //close socket
        //stop background tasks
        boolean success = true;
//        stop = true;

        MyClientTaskTimer.cancel();
        try {
            mSensorManager.unregisterListener(this, mAcc);
            mSensorManager.unregisterListener(this, mMag);
        } catch (Exception e){
            e.printStackTrace();
//            return false;
            success = false;
        }
        new MyClientTask(IpAddress, Integer.parseInt(port), "close", socket);
        try {
            if (socket != null) {
                if (!socket.isClosed()) {
                    socket.close();
                }
            }
        } catch (ConnectException e){
            e.printStackTrace();
//            return false;
            success = false;
        } catch (IOException e){
            e.printStackTrace();
//            return false;
            success = false;
        }
        mWebView.destroy();
        return success;

    }

    private boolean startup(){
        try{
            stop = false;
            toastMade = false;
            mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
            mWebView.setVisibility(View.VISIBLE);
//            LoadImageRepeatedly();
//            MyClientTaskRepeated();
            MyClientTaskTimer.scheduleAtFixedRate(MyClientTaskTimertask,0,100);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
