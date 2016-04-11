package com.example.justin.falcontest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.justin.falcontest.MESSAGE";
    SharedPreferences settings_prefs;
    String IpAddress;
    String Username;
    String Password;
    String Port;
    TextView IP_editor;
    TextView User_editor;
    TextView Pass_editor;
    TextView Port_editor;
//    private SensorManager mSensorManager;
//    private Sensor mAcc, mGyr, mMag;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //when button is clicked, update text to have the new IP address.
        //this is just to confirm that the change went through
        final Button setIPBtn = (Button) findViewById(R.id.setIPButton);
        final EditText setIpAddr = (EditText) findViewById(R.id.setIP);
        final Button setUserBtn = (Button) findViewById(R.id.setUserButton);
        final EditText setUsername = (EditText) findViewById(R.id.editUsername);
        final Button setPassBtn = (Button) findViewById(R.id.setPassButton);
        final EditText setPassword = (EditText) findViewById(R.id.editPassword);
        final EditText setPort = (EditText) findViewById(R.id.editPort);
        final Button setPortBtn = (Button) findViewById(R.id.setPortButton);


        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        Username = settings_prefs.getString("Username", "");
        final SharedPreferences.Editor edit_user = settings_prefs.edit();
        Password = settings_prefs.getString("Password", "");
        final SharedPreferences.Editor edit_pass = settings_prefs.edit();
        Port = settings_prefs.getString("Port", "");
        final SharedPreferences.Editor edit_port = settings_prefs.edit();
        //to edit settings_pref: edit_ip.putString("IP", value_of_variable).commit();

        IP_editor = (TextView) findViewById(R.id.setIP);
        IP_editor.setText(IpAddress);

        User_editor = (TextView) findViewById(R.id.editUsername);
        User_editor.setText(Username);

        Pass_editor = (TextView) findViewById(R.id.editPassword);
        Pass_editor.setText(Password);

        Port_editor = (TextView) findViewById(R.id.editPort);
        Port_editor.setText(Port);

        setIPBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //save IP address to shared prefference variable
                edit_ip.putString("IP", setIpAddr.getText().toString()).commit();

            }
        });

        setUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save username to shared preferrence variable
                edit_user.putString("Username", setUsername.getText().toString()).commit();
            }
        });

        setPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save password to shared preferrence variable
                edit_pass.putString("Password", setPassword.getText().toString()).commit();
            }
        });

        setPortBtn.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                edit_port.putString("Port", setPort.getText().toString()).commit();
            }
        });

//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_FASTEST);
//        Log.i("sensors", "Sensors created");
//        List<Sensor> mSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//
//        for (Sensor sensor: mSensors){
//            Log.i("SensorListing", "name:\t" + sensor.getName() + "\tType:\t"+sensor.getType());
//        }


    }

    @Override
    protected void onResume(){
        super.onResume();

        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");

        IP_editor = (TextView) findViewById(R.id.setIP);
        IP_editor.setText(IpAddress);
    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        Log.i("sensors", "Sensor Changed");
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.i("sensors", "Accuracy Changed");
//    }
}
