package com.example.justin.falcontest;

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

import android.os.AsyncTask;

public class SocketClient extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView textResponse;
    EditText editTextAddress, editTextPort, editData;
    Button buttonConnect, buttonClear;
    String data;



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
        textResponse = (TextView)findViewById(R.id.responseSocket);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonConnect.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                MyClientTask myClientTask = new MyClientTask(
                        editTextAddress.getText().toString(),
                        Integer.parseInt(editTextPort.getText().toString()),
                        editData.getText().toString());
                myClientTask.execute();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});




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
        }
    };

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String dataToSend;

        MyClientTask(String addr, int port, String msg) {
            dstAddress = addr;
            dstPort = port;
            dataToSend = msg;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                // Socket Code!!!
                socket = new Socket(dstAddress, dstPort);

                OutputStream toServer = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(toServer);
//                out.writeUTF("Hello from " + socket.getLocalSocketAddress());
                out.writeUTF(dataToSend);
                InputStream fromServer = socket.getInputStream();
                DataInputStream in = new DataInputStream(fromServer);
                response = in.readUTF();

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
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
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

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
