package com.example.justin.falcontest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.justin.falcontest.MESSAGE";
    SharedPreferences settings_prefs;
    String IpAddress;
    String Username;
    String Password;
    TextView IP_editor;
    TextView User_editor;
    TextView Pass_editor;

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


        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        Username = settings_prefs.getString("Username", "");
        final SharedPreferences.Editor edit_user = settings_prefs.edit();
        Password = settings_prefs.getString("Password", "");
        final SharedPreferences.Editor edit_pass = settings_prefs.edit();
        //to edit settings_pref: edit_ip.putString("IP", value_of_variable).commit();

        IP_editor = (TextView) findViewById(R.id.setIP);
        IP_editor.setText(IpAddress);

        User_editor = (TextView) findViewById(R.id.editUsername);
        User_editor.setText(Username);

        Pass_editor = (TextView) findViewById(R.id.editPassword);
        Pass_editor.setText(Password);

        setIPBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //save IP address to shared prefference variable
                edit_ip.putString("IP", setIpAddr.getText().toString()).commit();

            }
        });

        setUserBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
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


    }

    @Override
    protected void onResume(){
        super.onResume();

        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");

        IP_editor = (TextView) findViewById(R.id.setIP);
        IP_editor.setText(IpAddress);
    }

}
