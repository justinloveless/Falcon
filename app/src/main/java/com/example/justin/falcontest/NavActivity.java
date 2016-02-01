package com.example.justin.falcontest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.TextView;

import static com.example.justin.falcontest.SSH.executeRemoteCmd;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences mPrefs;
    String IpAddress;
    TextView IpChecker;
    TextView SSHResponse;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Shared variables
        mPrefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = mPrefs.getString("IP", "xxx.xxx.xxx.xxx");

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
        try {
            SSHResponse.setText( SSH.executeRemoteCmd("pi", "raspberry", IpAddress,22,"ls"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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
