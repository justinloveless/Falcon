package com.example.justin.falcontest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;

public class SocketClient extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences settings_prefs;
    String IpAddress;
    String port;
    TextView textResponse;
    EditText editTextAddress, editTextPort, editData;
    Button buttonConnect, buttonClear, buttonSend, buttonClose;
    String data;
    Socket socket = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_socket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextAddress = (EditText)findViewById(R.id.addressSocket);
        editTextPort = (EditText)findViewById(R.id.portSocket);
        editData = (EditText) findViewById(R.id.dataSocket);
        buttonConnect = (Button)findViewById(R.id.connectSocket);
        buttonClear = (Button)findViewById(R.id.clearSocket);
        buttonSend = (Button)findViewById(R.id.sendSocket);
        buttonClose = (Button)findViewById(R.id.closeSocket);
        textResponse = (TextView)findViewById(R.id.responseSocket);

        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        port = settings_prefs.getString("PORT", "8888");
        final SharedPreferences.Editor edit_port = settings_prefs.edit();
        editTextAddress.setText(IpAddress);
        editTextPort.setText(port);


        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonConnect.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                MyClientTask myClientTask = new MyClientTask(
                        editTextAddress.getText().toString(),
                        Integer.parseInt(editTextPort.getText().toString()),
                        editData.getText().toString());
                if (myClientTask.getStatus() == AsyncTask.Status.RUNNING){
                    myClientTask.cancel(true);
                }
                else {
                    myClientTask.execute();
                }

//                MyClientTaskRepeated();

                edit_port.putString("PORT", editTextPort.getText().toString()).commit();
                edit_ip.putString("IP", editTextAddress.getText().toString()).commit();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});

        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){

            }
        });




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
    }

    View.OnClickListener buttonConnectOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            MyClientTask myClientTask = new MyClientTask(
                    editTextAddress.getText().toString(),
                    Integer.parseInt(editTextPort.getText().toString()),
                    editData.getText().toString());
            myClientTask.execute();

//            MyClientTaskRepeated();
        }
    };
    public String getDataToSend(){
        return editData.getText().toString();
    }
    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String dataToSend;

        MyClientTask(String addr, int port, String msg) {
            dstAddress = addr;
            dstPort = port;
//            dataToSend = msg;
            dataToSend = msg;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                // Socket Code!!!
                socket = new Socket(dstAddress, dstPort);
                //loop through this
                while (dataToSend != "close") {
                    Log.d("dataSent", dataToSend);
                    OutputStream toServer = socket.getOutputStream();
                    DataOutputStream out = new DataOutputStream(toServer);
//                    out.writeUTF("Hello from " + socket.getLocalSocketAddress());
                    out.writeUTF(dataToSend + "\n");
                    Thread.sleep(1000);
//                    InputStream fromServer = socket.getInputStream();
//                    DataInputStream in = new DataInputStream(fromServer);
//                    response = in.readUTF();
                    dataToSend = getDataToSend();
                }

                socket.close();
//                ByteArrayOutputStream byteArrayOutputStream =
//                        new ByteArrayOutputStream(1024);
//                byte[] buffer = new byte[1024];
//
//                int bytesRead;
//                InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     * Include timeout to fix this
     */
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    byteArrayOutputStream.write(buffer, 0, bytesRead);
//                    response += byteArrayOutputStream.toString("UTF-8");
//                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            finally {
//                if (socket != null) {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
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
            Intent intent = new Intent(SocketClient.this, Picam.class);
            String IpAddr = "@string/IPAddress";
            intent.putExtra("IP", IpAddr);
            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            Intent intent = new Intent(SocketClient.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
            Intent intent = new Intent(SocketClient.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_bluetooth) {
            // Handle the web socket activity
            Intent intent = new Intent(SocketClient.this, BluetoothLE.class);
            startActivity(intent);

        } else if (id == R.id.nav_home) {
            Intent intent = new Intent(SocketClient.this, NavActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void MyClientTaskRepeated() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    new MyClientTask(editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()),
                            editData.getText().toString()
                    ).execute();
                }
                catch (Exception e){

                }
            }
        }, 0 , 1000);
    }
}
