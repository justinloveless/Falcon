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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import android.widget.Button;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.AsyncTask;
import android.widget.Toast;


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
    public ArrayList<String> sArray = new ArrayList<>();
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
        port = settings_prefs.getString("Port", "8888");

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

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
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
                filters = new ArrayList<>();
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
            Log.e("onDestroy", e.getMessage());
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
//            Log.i("callbackType", String.valueOf(callbackType));
//            Log.i("result", result.toString());
            if (result.getScanRecord().getDeviceName() != null) {
                devName = result.getScanRecord().getDeviceName();
                if (!existsInArray(sArray, devName)){//only unique values
                    //add new string to array
                    sArray.add(devName);
                }
            }
            else {
                devName = "null";
            }

//            Log.i("device Name", devName);

            //connect to CC2650 SensorTag
            if (devName.matches("CC2650 SensorTag")) {
//                Log.d("connecting", "Connecting to device: "+ devName);
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
        public double a_x =0;
        public double a_y=0;
        public double a_z=0;
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
            resetRWState();
            SensorTagCommunication();
        }


        public void SensorTagCommunication(){
            String TAG = "SensorTagComms";
            BluetoothGattService mSvc;
            Log.d(TAG, "state: " + mReadWriteState);
            BluetoothGattCharacteristic mCh;
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
                    advanceRW();
                    SensorTagCommunication();
                    break;
                case 3: //read data
                    mSvc = mServices.get(6);
                    mCh = mSvc.getCharacteristic(UUID.fromString("f000aa81-0451-4000-b000-000000000000"));
                    Log.d(TAG, "" + mGatt.readCharacteristic(mCh));
                    advanceRW();
                    break;
                case 4: //empty state
                    MyClientTaskTimer.scheduleAtFixedRate(MyClientTaskTimertask, 0, 100);
                    break;
                default:
                    resetRWState();//go to initial state
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().toString().equals("f000aa81-0451-4000-b000-000000000000") ) {
                byte[] val = characteristic.getValue();
                a_x = (((val[7] << 8) + val[6]) / (32768.0 / 2.0)) * -1;
                a_y = ((val[9] << 8) + val[8]) / (32768.0 / 2.0);
                a_z = (((val[11] << 8) + val[10]) / (32768.0 / 2.0)) * -1;
                //calculate roll
                Roll = (Math.atan2(a_y, a_z)*180/Math.PI);
                //calculate pitch
                Pitch = (Math.atan2(a_x, Math.sqrt(a_y*a_y + a_z*a_z)) *180/Math.PI);


                //map Pitch to pitch command values
                Pitch = dPitchMin + ((dPitchMax - dPitchMin)/(pitchMax - pitchMin))*(Pitch - pitchMin);
                pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + Pitch/avgRange; // calculate exponential average
                sPitch = String.format(Locale.US, "%.0f", pitchAvg);


                if ( Roll > 10 || Roll < -10){// roll control
                    if (Roll > rollMax)
                        Roll = rollMax;
                    else if (Roll < rollMin)
                        Roll = rollMin;
                    Roll = dRollMin + ((dRollMax - dRollMin)/(rollMax - rollMin))*(Roll - rollMin);
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + Roll/avgRange; // calculate exponential average
                    sRoll = String.format(Locale.US, "%.0f", rollAvg);
                }else {
                    Roll = (dRollMax - dRollMin)/2 + dRollMin;
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + Roll/avgRange; // calculate exponential average
                    sRoll = String.format(Locale.US, "%.0f", rollAvg);
                }
                sensorData = (sRoll + "," + sPitch + "," + sThrottle + "," + sYaw);
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
            //repeatedly read Movement Sensor
            mGatt.readCharacteristic(characteristic);
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
            String sent = "";
            for (byte aVal : val) {
                sent += "[" + aVal + "] ";
            }
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
            gravity = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            geomag = event.values.clone();
        }
        if (gravity != null && geomag != null){ //wait until the first time the sensors are gathered
            boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);
            if (success) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientVals);
                double mAzimuth = Math.toDegrees(orientVals[0]);
                double mPitch = Math.toDegrees(orientVals[1])*-1.0;
                sYaw = String.format(Locale.US,"%.3f", mAzimuth);
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
                sThrottle = String.format(Locale.US, "%.0f", throttleAvg);
                //yaw control
                //TODO implement a BANG-BANG yaw control
                mAzimuth = dYawMin + ((dYawMax - dYawMin)/(yawMax - yawMin))*(mAzimuth - yawMin);
                yawAvg = yawAvg*(1.0 - 1.0/avgRange) + mAzimuth/avgRange; // calculate exponential average
                sYaw = String.format(Locale.US, "%.0f", yawAvg);
                sensorData = (sRoll + "," + sPitch + "," + sThrottle + "," + sYaw);
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
                        if (socket.isClosed())
                            Log.d("myClientTask", "new socket failed to be created");
                    } catch (ConnectException e){
                        if (!toastMade) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Cool your jets!\nFalcon is not ready", Toast.LENGTH_SHORT).show();
                                }
                            });
                            toastMade = true;
                        }

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
                            out.writeUTF(dataToSend + "\n");
                        } else {
                            socket.close();
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

        if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, IpSettings.class);
            startActivity(intent);

        } else if (id == R.id.nav_home) {
            cleanup();
            Intent intent = new Intent(SocketClient.this, NavActivity.class);
            startActivity(intent);

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


    private boolean cleanup (){
        //close socket
        //stop background tasks
        boolean success = true;
        stop = true;
        MyClientTaskTimer.cancel();
        try {
            mSensorManager.unregisterListener(this, mAcc);
            mSensorManager.unregisterListener(this, mMag);
        } catch (Exception e){
            e.printStackTrace();
            success = false;
        }
        new MyClientTask(IpAddress, Integer.parseInt(port), "close", socket);
        try {
            if (socket != null) {
                if (!socket.isClosed()) {
                    socket.close();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
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
            MyClientTaskTimer.scheduleAtFixedRate(MyClientTaskTimertask,0,100);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
