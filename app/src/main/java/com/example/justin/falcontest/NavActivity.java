package com.example.justin.falcontest;

import android.content.Intent;
import android.content.SharedPreferences;
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
        implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences mPrefs;
    String IpAddress;
    String Username;
    String Password;
    TextView IpChecker;
    TextView SSHResponse;
    String RPiResponse = "original";
    EditText edit_cmd;

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

        SSHResponse = (TextView) findViewById(R.id.SSHResponse);
        //enable textview to be scrollable if contents are too long
        SSHResponse.setMovementMethod(new ScrollingMovementMethod());
        try {
            SSHResponse.setText( SSH.executeRemoteCmd(edit_cmd.getText().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                            SSH.connect(Username,Password,IpAddress, 22);
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
            Intent intent = new Intent(NavActivity.this, Picam.class);
            String IpAddr = "@string/IPAddress";
            intent.putExtra("IP", IpAddr);
            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            Intent intent = new Intent(NavActivity.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
            Intent intent = new Intent(NavActivity.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_bluetooth) {
            // Handle the web socket activity
            Intent intent = new Intent(NavActivity.this, BluetoothLE.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_socket){
            // Handle the web socket activity
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
    }

    @Override
    protected void onPause() {
       super.onPause();
        //what to do when another activity is partially obstructing this one

    }

    @Override
    protected void onStop(){
        super.onStop();
        //what to do when another activity is completely obstructing this one
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //happens immediately after user goes back to activity
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //happens immediately before activity is ended
    }
}
