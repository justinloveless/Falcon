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
    TextView IP_editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //when button is clicked, update text to have the new IP address.
        //this is just to confirm that the change went through
        final Button setIPBtn = (Button) findViewById(R.id.setIPButton);
        final EditText setIpAddr = (EditText) findViewById(R.id.setIP);


        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        //to edit settings_pref: edit_ip.putString("IP", value_of_variable).commit();

        IP_editor = (TextView) findViewById(R.id.setIP);
        IP_editor.setText(IpAddress);

        setIPBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                edit_ip.putString("IP", setIpAddr.getText().toString()).commit();

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
