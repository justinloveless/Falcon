package com.example.justin.falcontest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

//import com.example.justin.falcontest.common.BluetoothLeService;

import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class BluetoothLE extends AppCompatActivity
public class BluetoothLE extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 5000; //5 seconds
    private BluetoothLeScanner mLEScanner;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    public ScanSettings settings;
    public ListView serviceList;
    public String[] serviceArray = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8",
        "Item 9", "Item 10", "Item 11", "Item 12", "Item 13", "Item 14", "Item 15"};
    public ArrayList<String> sArray = new ArrayList<String>();
    public ArrayAdapter ServiceAdapter;
    public TextView deviceName;
    public TextView description;
    public Button scan;
    public String devName;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public List<BluetoothGattService> mServices;
    public BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_le);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        scan = (Button) findViewById(R.id.scanBtn);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                sArray.clear();
                ServiceAdapter.notifyDataSetChanged();
                serviceList.invalidateViews();
                //start scannin for devices:
                scanLeDevice(true);
            }
        });

        sArray.add("Item 1");
        ServiceAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, sArray);
        serviceList = (ListView) findViewById(R.id.serviceList);
        serviceList.setAdapter(ServiceAdapter);
        //add new string to array
        sArray.add("Item 2");
        //update content
        ServiceAdapter.notifyDataSetChanged();
        //redraw view
        serviceList.invalidateViews();


        //***********Bluetooth stuff**************//
        //check if BLE is supported
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
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

        // turn on location services if they aren't already (forcefully, muahaha)
        //  (BLE scan won't work without GPS on)
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        description = (TextView)findViewById(R.id.textView2);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_le, menu);
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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_webview) {
            // Handle the webview activity
            Intent intent = new Intent(BluetoothLE.this, Picam.class);
            String IpAddr = "@string/IPAddress";
            intent.putExtra("IP", IpAddr);
            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            Intent intent = new Intent(BluetoothLE.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
            Intent intent = new Intent(BluetoothLE.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_home) {
            // Handle the web socket activity
            Intent intent = new Intent(BluetoothLE.this, NavActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
        }
        catch (Exception e) {
            Log.d("PauseException", e.getMessage().toString());
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {

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
                    //update content
                    ServiceAdapter.notifyDataSetChanged();
                    //redraw view
                    serviceList.invalidateViews();
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
//            try {
//                deviceName.setText(device.getName());
//                deviceName.setText(devName);
//            } catch (Exception e) {
//                Log.e("ConnectToDevice", "Unable to set name. Device: " + device.toString()
//                        + "\nName: " + device.getName()
//                        + "\nError: " + e.getMessage() + " at line " + e.getStackTrace()[2].getLineNumber());
//                e.printStackTrace();
//            }
        }
//        else {
//            Log.e("connecToDevice", "mGatt not null: " + mGatt.toString());
//            scanLeDevice(false);
//        }
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
//            String uuidVal = characteristic.getUuid().toString();
//            Log.i("Notice"," "+SensorTagGatt.lookup(uuidVal, "notFound")+" = "+ uuidVal);
//            if (characteristic.getUuid().toString().equals("f000aa82-0451-4000-b000-000000000000")) {
            curNot = System.currentTimeMillis();
            if (curNot - prevNot < 75){
                Log.e("QuickNotice", "Notification happened too quickly");
            }
            prevNot = curNot;
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
                Log.i("Notice", String.format("\tGyr: \t%.2f\t%.2f\t%.2f\tAcc: \t%.2f\t%.2f\t%.2f\tMag: \t%.2f\t%.2f\t%.2f"
                        , g_x, g_y, g_z
                        , a_x, a_y, a_z
                        , m_x, m_y, m_z));

//            }else if (characteristic.getUuid().toString().equals("f000ccc1-0451-4000-b000-000000000000")){
//                connParamWritten = true;
//            }

//            if (gatt.getConnectionState(gatt.getDevice()) == BluetoothProfile.STATE_DISCONNECTED){
//            if (bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED){
//                Log.e("gattCallback", "STATE_DISCONNECTED");
//                connectToDevice(gatt.getDevice());
//                Log.e("gattCallback", "reconnecting");
//            }
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
                    mSvc = mServices.get(6);
                    mCh = mSvc.getCharacteristic(
                            UUID.fromString("f000aa81-0451-4000-b000-000000000000"));
                    mDes = mCh.getDescriptors().get(0);
                    mDes.setValue(new byte[] {0x01, 0x00}); //enable remotely
                    mGatt.setCharacteristicNotification(mCh, true); // enable locally
                    Log.d(TAG, "" + mGatt.writeDescriptor(mDes));
                    prevNot = System.currentTimeMillis();
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
                    break;
                default:
                    resetRWState();//go to initial state
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("CharRead", "reading Characteristic");
            if (characteristic.getUuid().toString().equals("f000aa81-0451-4000-b000-000000000000") ) {
                byte[] val = characteristic.getValue();
                char[] val_s = characteristic.getValue().toString().toCharArray();
                g_x = ((val[1] << 8) + val[0]) / 128.0;
                g_y = ((val[3] << 8) + val[2]) / 128.0;
                g_z = ((val[5] << 8) + val[4]) / 128.0;
                a_x = (((val[7] << 8) + val[6]) / (32768.0 / 2.0)) * -1;
                a_y = ((val[9] << 8) + val[8]) / (32768.0 / 2.0);
                a_z = (((val[11] << 8) + val[10]) / (32768.0 / 2.0)) * -1;
                //magnetometer conversion is done on chip, so no calculation should be needed
                m_x = 1.0 * ((val[13] << 8) + val[12])/* / (32768 / 4912)*/;
                m_y = 1.0 * ((val[15] << 8) + val[14]) /*/ (32768 / 4912)*/;
                m_z = 1.0 * ((val[17] << 8) + val[16])/* / (32768 / 4912)*/;
                Log.i("value", "\tGyr: \t" + g_x + "\t,\t" + g_y + "\t,\t" + g_z
                        + "\tAcc: \t" + a_x + "\t,\t" + a_y + "\t,\t" + a_z
                        + "\tMag: \t" + m_x + "\t,\t" + m_y + "\t,\t" + m_z);
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
//            mGatt.readCharacteristic(characteristic);
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
            Log.i("onCharacteristicWrite", "bytes written:" + val.length + " {"+sent +"} to " + characteristic.getUuid().toString());
            advanceRW();
            SensorTagCommunication();
        }
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i("onReliableWrite", " well, that just happened... ");
        }
    };



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



    public boolean existsInArray (ArrayList<String> array, String s) {
        for (String str : array){
            if (s.equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices){
        if(gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();

        for (BluetoothGattService gattService : gattServices){
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("NAME", SensorTagGatt.lookup(uuid, unknownServiceString));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        "NAME", SensorTagGatt.lookup(uuid, unknownCharaString));
                currentCharaData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }


    }


}
