package com.example.justin.falcontest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {
    SharedPreferences mPrefs;
    String IpAddress;
    String Username;
    String Password;
    TextView IpChecker;
    TextView SSHResponse;
    String RPiResponse = "original";
    EditText edit_cmd;
    private SensorManager mSensorManager;
    private Sensor mAcc, mGyr, mMag, mRot;
    TextView pitch, roll, throttle, yaw;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edit_cmd = (EditText) findViewById(R.id.editCommand);

        //Shared variables
        mPrefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = mPrefs.getString("IP", "xxx.xxx.xxx.xxx");
        Username = mPrefs.getString("Username", "pi");
        Password = mPrefs.getString("Password", "raspberry");

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

        IpChecker = (TextView) findViewById(R.id.NavIpChecker);
        IpChecker.setText(IpAddress);

//        SSHResponse = (TextView) findViewById(R.id.SSHResponse);
        //enable textview to be scrollable if contents are too long
//        SSHResponse.setMovementMethod(new ScrollingMovementMethod());
//        try {
//            SSHResponse.setText( SSH.executeRemoteCmd(edit_cmd.getText().toString()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        final Button SendBtn = (Button) findViewById(R.id.SendCmd);

        SendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                final String cmd = edit_cmd.getText().toString();
                new AsyncTask<Integer, Void, Void>(){

                    protected String onProgressUpdate (String... values){
                        RPiResponse = "Progress Update";
                        return RPiResponse;
                    }

                    protected Void doInBackground(Integer... params){
                        try {
                           RPiResponse = SSH.executeRemoteCmd(cmd );
                            publishProgress();

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("Exception", "    exception occured");

                            RPiResponse = "Exception";
                        }
                        return null;
                    }

                    protected  void onPostExecute(Void v){
                        SSHResponse.setText(RPiResponse);
                    }
                }.execute();
                //SSHResponse.setText(RPiResponse);
            }
        });

        final Button connect = (Button) findViewById(R.id.connectButton);

        connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                new AsyncTask<Integer, Void, Void>(){
                    protected Void doInBackground(Integer... params){
                        try {
                            SSH.connect(Username, Password, IpAddress, 22);
                            RPiResponse = "Connected to " + IpAddress;

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("Exception", "    exception occured");
                            RPiResponse = "Could not Connect";
                        }
                        return null;
                    }

                    protected  void onPostExecute(Void v){
                        SSHResponse.setText(RPiResponse);
                    }
                }.execute();
            }
        });

        final Button disconnect = (Button) findViewById(R.id.disconnectButton);

        disconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new AsyncTask<Integer, Void, Void>(){
                    protected Void doInBackground(Integer... params){
                        try {
                            SSH.disconnect();
                            RPiResponse = "Disconnected from " + IpAddress;

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("Exception", "    exception occured");
                            RPiResponse = "Could not disconnect";
                        }
                        return null;
                    }

                    protected  void onPostExecute(Void v){
                        SSHResponse.setText(RPiResponse);
                    }
                }.execute();
            }
        });

//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
//            Log.i("Sensors", "There is a gyroscope");
//        }
//        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
//            Log.i("Sensors", "There is an accelerometer");
//        }
//
//        pitch = (TextView) findViewById(R.id.textView3);
//        roll = (TextView) findViewById(R.id.textView4);
//        throttle = (TextView) findViewById(R.id.textView5);
//        yaw = (TextView) findViewById(R.id.textView6);

//        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);

    }

    float[] inR = new float[16];
    float[] outR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    private static final double dPitchMin = 1200;//drone pitch
    private static final double dPitchMax = 1900;
    private static final double dThrottleMin = 1200; //drone throttle
    private static final double dThrottleMax = 1900;
    private static final double pitchMin = -45; // pitch from phone controls drone pitch AND drone throttle
    private static final double pitchMax = 45;
    private static final double dRollMin = 1200; //drone roll
    private static final double dRollMax = 1900;
    private static final double rollMin = 45;
    private static final double rollMax = 135;
    private static final double dYawMin = 1200;
    private static final double dYawMax = 1900;
    private static final double yawMin = -180;
    private static final double yawMax = 180;
    @Override
    public void onSensorChanged(SensorEvent event){
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
        if (gravity != null && geomag != null){
            boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);
            if (success) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientVals);
                double mAzimuth = Math.toDegrees(orientVals[0]);
                double mPitch = Math.toDegrees(orientVals[1])*-1.0;
                double mRoll = Math.toDegrees(orientVals[2])*-1.0;
//                pitch.setText("pitch = " + String.format("%.3f", mPitch));
                roll.setText(String.format("%.3f",  mRoll));
                yaw.setText(String.format("%.3f", mAzimuth));
//                Log.i("orient", String.format("%.2f\t%.2f\t%.2f", mAzimuth, mPitch, mRoll));
                if (mPitch > 15 || mPitch < -15){//throttle control
                    if (mPitch > pitchMax)
                        mPitch = pitchMax;
                    else if (mPitch < pitchMin)
                        mPitch = pitchMin;
                    mPitch = dThrottleMin + ((dThrottleMax - dThrottleMin)/(pitchMax  - pitchMin))*(mPitch - pitchMin);
                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
                    mPitch = (dPitchMax - dPitchMin)/2 + dPitchMin;
                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
                }
                else { //pitch control
                    mPitch = dPitchMin + ((dPitchMax - dPitchMin)/(pitchMax - pitchMin))*(mPitch - pitchMin);
                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
                    mPitch = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
                }
//                Log.d("sensors", "roll = " + mRoll);
                if ( mRoll > 100 || mRoll < 80){// roll control
                    if (mRoll > rollMax)
                        mRoll = rollMax;
                    else if (mRoll < rollMin)
                        mRoll = rollMin;
//                    Log.d("sensors", "outter");
                    mRoll = dRollMin + ((dRollMax - dRollMin)/(rollMax - rollMin))*(mRoll - rollMin);
                    roll.setText("roll = " + String.format("%.0f", mRoll));
                }else {
//                    Log.d("sensors", "inner");
                    mRoll = (dRollMax - dRollMin)/2 + dRollMin;
                    roll.setText("roll = " + String.format("%.0f", mRoll));
                }
                //yaw control
                mAzimuth = dYawMin + ((dYawMax - dYawMin)/(yawMax - yawMin))*(mAzimuth - yawMin);
                yaw.setText("yaw = " + String.format("%.0f", mAzimuth));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
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
            //Go to settings activity
            Intent intent = new Intent(NavActivity.this, MainActivity.class);
            startActivity(intent);
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
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            Intent intent = new Intent(NavActivity.this, Picam.class);
            String IpAddr = "@string/IPAddress";
            intent.putExtra("IP", IpAddr);
            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            Intent intent = new Intent(NavActivity.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            Intent intent = new Intent(NavActivity.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_bluetooth) {
            // Handle the web socket activity
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            Intent intent = new Intent(NavActivity.this, BluetoothLE.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_socket){
            // Handle the web socket activity
//            mSensorManager.unregisterListener(this, mAcc);
//            mSensorManager.unregisterListener(this, mMag);
            Intent intent = new Intent(NavActivity.this, SocketClient.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //happens immediately after onCreate() or onRestart();

    }
    @Override
    protected void onResume() {
        super.onResume();
        //what to do when this activity starts up again
        IpAddress = mPrefs.getString("IP", "xxx.xxx.xxx.xxx");
        IpChecker = (TextView) findViewById(R.id.NavIpChecker);
        IpChecker.setText(IpAddress);
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
       super.onPause();
        //what to do when another activity is partially obstructing this one
//        mSensorManager.unregisterListener(this, mAcc);
//        mSensorManager.unregisterListener(this, mMag);

    }

    @Override
    protected void onStop(){
        super.onStop();
        //what to do when another activity is completely obstructing this one
//        mSensorManager.unregisterListener(this, mAcc);
//        mSensorManager.unregisterListener(this, mMag);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //happens immediately after user goes back to activity
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //happens immediately before activity is ended
//        mSensorManager.unregisterListener(this, mAcc);
//        mSensorManager.unregisterListener(this, mMag);
    }
}
