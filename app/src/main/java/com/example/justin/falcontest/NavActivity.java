package com.example.justin.falcontest;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY };
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "falconv.uh@gmail.com";
    SharedPreferences mPrefs;
    String localIp;
    String IpAddress;
    String Port;
    String HistId;
    String Username;
    String Password;
    SharedPreferences.Editor  edit_ip;
    SharedPreferences.Editor edit_port;
    SharedPreferences.Editor edit_hist;
    TextView IpChecker;
    TextView PortChecker;
    Button checkGmailButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Shared variables
        mPrefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = mPrefs.getString("IP", "xxx.xxx.xxx.xxx");
        edit_ip = mPrefs.edit();
        Port = mPrefs.getString("Port", "8888");
        edit_port = mPrefs.edit();
        HistId = mPrefs.getString("HistId", "0");
        edit_hist = mPrefs.edit();
        Username = mPrefs.getString("Username", "pi");
        Password = mPrefs.getString("Password", "raspberry");

        Log.d("NavActivity", "About to make credentials");
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        IpChecker = (TextView) findViewById(R.id.NavIpChecker);
        IpChecker.setText(IpAddress);
        PortChecker =(TextView) findViewById(R.id.PortChecker);
        PortChecker.setText(Port);


        checkGmailButton = (Button) findViewById(R.id.GmailCheckBtn);
        checkGmailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                checkGmailButton.setEnabled(false);
                getResultsFromApi();
                localIp = checkLocalIp();
                if (!localIp.startsWith("172.25.")){
                    runOnUiThread(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(),
                                    "!!!Please connect to UHWireless or UHVPN!!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    //TODO request connection to UHWireless or UHVPN
                }
                checkGmailButton.setEnabled(true);
            }
        });


    }

    private void getResultsFromApi(){
        if (! isGooglePlayServicesAvailable()){
            Log.d("getResultsFromApi", "GoglePlayServices aren't available");
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
                Log.d("getResultsFromApi", "No account is selected");
                chooseAccount();
                Log.d("getResultsFromApi", "Account is now: " + mCredential.getSelectedAccountName());
//            getResultsFromApi(); // re-call itself (THIS DOESN'T WORK EVEN THOUGH IT CAME FROM GOOGLE'S EXAMPLE CODE D:< )
            runOnUiThread(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),
                            "Account had to be selected, please try again :)",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }else if (! isDeviceOnline()) {
            Log.e("Gmail API", "No network connection available.");
        } else {
            Log.d("getResultsFromApi","About to Execute Request Task ");
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {

            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            Log.d("chooseAccount", "Account name=" + accountName);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
            Log.d("chooseAccount", "Does not have permission");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.e("onActivityResults", "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                NavActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Message> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build();
            Log.d("makeRequestTask", "In Constructor");
        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Message doInBackground(Void... params) {
            try {
                Log.d("makeRequestTask", "About to getDataFromApi");
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        private Message getDataFromApi() throws IOException {
            String user = "me";
            ListMessagesResponse messagesResponse = mService.users().messages().list(user).execute();
            return mService.users().messages().get(user, messagesResponse.getMessages().get(0).getId()).execute();
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Message output) {
            if (output == null || output.size() == 0) {
                Log.d("makeRequestTask", "No results returned");
            } else {
                String snippet = output.getSnippet();
                String data[] = snippet.split(" ");
                String IP;
                String port;
                if (data.length >= 2) {
                    IP = data[0];
                    port = data[1];
                } else {
                    IP = data[0];
                    port = "8888";
                }
                String histId = output.getHistoryId().toString();
                if (Integer.parseInt(histId) > Integer.parseInt(mPrefs.getString("HistId", "0"))){
                    edit_ip.putString("IP", IP).commit();
                    edit_hist.putString("HistId", histId).commit();
                    edit_port.putString("Port", port).commit();
                    PortChecker.setText(mPrefs.getString("Port", "8888"));
                    IpChecker.setText(mPrefs.getString("IP", "xxx.xxx.xxx.xxx"));
                    Log.d("makeRequestTask", "Updated Ip, historyId, and Port: " + mPrefs.getString("IP", "xxx.xxx.xxx.xxx")
                            + " " + mPrefs.getString("HistId", "0") + " " + mPrefs.getString("Port", "8888"));
                    runOnUiThread(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "Updated the settings!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Log.d("makeRequestTask", "Settings already up to date");
                    runOnUiThread(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "Settings are already up to date", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("makeRequestTask", "task was canceled for some reason");
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            NavActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("makeRequestTask", "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.d("makeRequestTask", "Request cancelled.");
            }
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
            Intent intent = new Intent(NavActivity.this, IpSettings.class);
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

        if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            Intent intent = new Intent(NavActivity.this, IpSettings.class);
            startActivity(intent);

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
        if (mCredential.getSelectedAccountName() == null){
            chooseAccount(); //get account name
        }
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


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Do nothing.
    }

    public String checkLocalIp(){
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
