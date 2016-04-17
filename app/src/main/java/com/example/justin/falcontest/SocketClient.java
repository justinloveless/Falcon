package com.example.justin.falcontest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.vrtoolkit.cardboard.CardboardView;

public class SocketClient extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener{

    SharedPreferences settings_prefs;
    String IpAddress;
    String port;
    TextView textResponse;
    TextView sensorVals;
    ImageView picam1;
    ImageView picam2;
    WebView mWebView;
//    EditText editTextAddress, editTextPort, editData;
    Button /*buttonConnect, buttonClear, buttonSend, */buttonClose;
    String data;
    Socket socket = null;
    SocketAddress sockAddr;
    private SensorManager mSensorManager;
    private Sensor mAcc, mGyr, mMag, mRot;
    private boolean stop;
    View decorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        decorView = getWindow().getDecorView();
//        decorView.setOnSystemUiVisibilityChangeListener(
//                new View.OnSystemUiVisibilityChangeListener(){
//                    @Override
//                    public void onSystemUiVisibilityChange(int i) {
//
//                    }
//                }
//        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        editTextAddress = (EditText)findViewById(R.id.addressSocket);
//        editTextPort = (EditText)findViewById(R.id.portSocket);
//        editData = (EditText) findViewById(R.id.dataSocket);
//        buttonConnect = (Button)findViewById(R.id.connectSocket);
//        buttonClear = (Button)findViewById(R.id.clearSocket);
//        buttonSend = (Button)findViewById(R.id.sendSocket);
        buttonClose = (Button)findViewById(R.id.closeSocket);
        textResponse = (TextView)findViewById(R.id.responseSocket);
        sensorVals = (TextView)findViewById(R.id.sensorData);
        mWebView = (WebView)findViewById(R.id.piviewer);

//        picam1 = (ImageView)findViewById(R.id.picam);
//        picam2 = (ImageView)findViewById(R.id.piCam2);


        settings_prefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = settings_prefs.getString("IP", "xxx.xxx.xxx.xxx");
        final SharedPreferences.Editor  edit_ip = settings_prefs.edit();
        port = settings_prefs.getString("Port", "8888");
        final SharedPreferences.Editor edit_port = settings_prefs.edit();
//        editTextAddress.setText(IpAddress);
//        editTextPort.setText(port);



        //Enable Javascript
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);//force links and redirects to open in web view instead of browser
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("http://" + IpAddress + "/piviewer");

//        buttonConnect.setOnClickListener( new View.OnClickListener(){
//            @Override
//            public void onClick(View arg0) {
//                MyClientTask myClientTask = new MyClientTask(
//                        editTextAddress.getText().toString(),
//                        Integer.parseInt(editTextPort.getText().toString()),
//                        editData.getText().toString());
//                if (myClientTask.getStatus() == AsyncTask.Status.RUNNING){
//                    myClientTask.cancel(true);
//                }
//                else {
//                    myClientTask.execute();
//                }
//
//                MyClientTaskRepeated();
//
//                edit_port.putString("Port", editTextPort.getText().toString()).commit();
//                edit_ip.putString("IP", editTextAddress.getText().toString()).commit();
//            }
//        });

//        buttonClear.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                textResponse.setText("");
//            }});

//        buttonSend.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick (View v) {
//
//            }
//        });

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
                if (!stop) {
                    cleanup();
                    buttonClose.setText("Restart");
                }else {
                    startup();
                    buttonClose.setText("Stop");
                }
            }
        });




//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            Log.i("Sensors", "There is a gyroscope");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Log.i("Sensors", "There is an accelerometer");
        }

//        pitch = (TextView) findViewById(R.id.textView3);
//        roll = (TextView) findViewById(R.id.textView4);
//        throttle = (TextView) findViewById(R.id.textView5);
//        yaw = (TextView) findViewById(R.id.textView6);

        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);

//        new LoadImage().execute("http://www.tehcute.com/pics/201110/marshmellow-kitten-big.jpg");
//        new SocketConnect(IpAddress, Integer.parseInt(editTextPort.getText().toString()));
//        LoadImageRepeatedly();
        MyClientTaskRepeated();

    }


    @Override
    protected void onStart() {
        super.onStart();
        //happens immediately after onCreate() or onRestart();

    }
    @Override
    protected void onResume() {
        super.onResume();
        startup();
    }

    @Override
    protected void onPause() {
        cleanup();
        super.onPause();
    }

    @Override
    protected void onStop(){
        cleanup();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startup();
    }

    @Override
    protected void onDestroy(){
        cleanup();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

//    public class LoadImage extends AsyncTask <String, Void, Bitmap>{
//        @Override
//        protected Bitmap doInBackground(String ... params) {
//            try {
//                InputStream is = (InputStream) new URL(params[0]).getContent();
////                Drawable d = Drawable.createFromStream(is, null);
//                Bitmap orig = BitmapFactory.decodeStream(is);
////                Bitmap targetL = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Canvas canvas = new Canvas(targetL);
////                RectF rectF = new RectF(0,0,1200,1080);
////                Path path = new Path();
////                path.addRect(rectF, Path.Direction.CW);
////                canvas.clipPath(path);
////                canvas.drawBitmap(orig, new Rect(0, 0, orig.getWidth(), orig.getHeight()),
////                        new Rect(0, 0, targetL.getWidth(), targetL.getHeight()), new Paint());
////                Matrix m = new Matrix();
////                m.postScale(1f, 1f);
////                Bitmap resized = Bitmap.createBitmap(targetL, 0,0,1500,1080, m, true);
////                BitmapDrawable bd = new BitmapDrawable(orig);
//                return orig;
//            }catch (Exception e) {
//                return null;
//            }
//        }
//        @Override
//        protected void onPostExecute(Bitmap d){
//            if (d != null){
////                Bitmap targetL = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Bitmap targetR = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
////                Canvas canvasL = new Canvas(targetL);
////                Canvas canvasR = new Canvas(targetR);
////                RectF rectFL = new RectF(0, 0, 1200, 1080);
////                RectF rectFR = new RectF(720, 0, 1920, 1080);
////                Path pathL = new Path();
////                Path pathR = new Path();
////                pathL.addRect(rectFL, Path.Direction.CW);
////                pathR.addRect(rectFR, Path.Direction.CW);
////                canvasL.clipPath(pathL);
////                canvasR.clipPath(pathR);
////                canvasL.drawBitmap(d, new Rect(0, 0, d.getWidth(), d.getHeight()),
////                        new Rect(0, 0, targetL.getWidth(), targetL.getHeight()), new Paint());
////                canvasR.drawBitmap(d, new Rect(0, 0, d.getWidth(), d.getHeight()),
////                        new Rect(0, 0, targetR.getWidth(), targetR.getHeight()), new Paint());
////                Matrix m = new Matrix();
////                m.postScale(1f,1f);
////                Bitmap resizedL = Bitmap.createBitmap(targetL, 0,0,1920,1080, m, true);
////                Bitmap resizedR = Bitmap.createBitmap(targetR, 0,0,1920,1080, m, true);
////                BitmapDrawable bdL = new BitmapDrawable(resizedL);
////                BitmapDrawable bdR = new BitmapDrawable(resizedR);
////                picam1.setImageDrawable(bdL);
////                picam2.setImageDrawable(bdR);
//                BitmapDrawable bd = new BitmapDrawable(d);
//                picam1.setImageDrawable(bd);
//                picam2.setImageDrawable(bd);
//            } else {
//                Toast.makeText(SocketClient.this, "Image doesn't exist, or network error", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    View.OnClickListener buttonConnectOnClickListener = new View.OnClickListener(){
//
//        @Override
//        public void onClick(View arg0) {
////            MyClientTask myClientTask = new MyClientTask(
////                    editTextAddress.getText().toString(),
////                    Integer.parseInt(editTextPort.getText().toString()),
////                    editData.getText().toString(),
////                    socket);
////            myClientTask.execute();
//
////            MyClientTaskRepeated();
//        }
//    };
    public String getDataToSend(){
//        return editData.getText().toString();
        return sensorData;
    }


    float[] inR = new float[16];
    float[] outR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] geomag = new float[3];
    float[] orientVals = new float[3];
    private static final double dPitchMin = 1300;//drone pitch
    private static final double dPitchMax = 1600;
    private static final double dThrottleMin = 1250; //drone throttle
    private static final double dThrottleMax = 1900;
    private static final double dThrottleNom = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
    private static final double pitchMin = -30; // pitch from phone controls drone pitch AND drone throttle
    private static final double pitchMax = 30;
    private static final double dRollMin = 1400; //drone roll
    private static final double dRollMax = 1700;
    private static final double rollMin = 45;
    private static final double rollMax = 135;
    private static final double dYawMin = 1400;
    private static final double dYawMax = 1700;
    private static final double yawMin = -180;
    private static final double yawMax = 180;
    private static final double avgRange = 15;
    private double pitchAvg, rollAvg, throttleAvg, yawAvg;
    private double yawInit;
    String sPitch, sRoll, sThrottle, sYaw, sensorData;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gravity == null || geomag == null){
            pitchAvg = rollAvg = yawAvg = throttleAvg = 0;
        }
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
        if (gravity != null && geomag != null){ //wait until the first time the sensors are gathered
            boolean success = SensorManager.getRotationMatrix(inR, I, gravity, geomag);
            if (success) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientVals);
                double mAzimuth = Math.toDegrees(orientVals[0]);
                double mPitch = Math.toDegrees(orientVals[1])*-1.0;
                double mRoll = Math.toDegrees(orientVals[2])*-1.0;
//                pitch.setText("pitch = " + String.format("%.3f", mPitch));
//                roll.setText(String.format("%.3f",  mRoll));
                sRoll = String.format("%.3f",  mRoll);
//                yaw.setText(String.format("%.3f", mAzimuth));
                sYaw = String.format("%.3f", mAzimuth);
//                Log.i("orient", String.format("%.2f\t%.2f\t%.2f", mAzimuth, mPitch, mRoll));
                if (mPitch > 15 || mPitch < -15){//throttle control
                    if (mPitch > pitchMax)
                        mPitch = pitchMax;
                    else if (mPitch < pitchMin)
                        mPitch = pitchMin;
                    if (mPitch > 15){
                        mPitch = dThrottleNom
                                + ((dThrottleMax - dThrottleNom)/(pitchMax  - 15))*(mPitch - 15);
                    }else if (mPitch < -15){
                        mPitch = dThrottleMin
                                + ((dThrottleNom - dThrottleMin)/(-15  - pitchMin))*(mPitch - pitchMin);
                    }
//                    mPitch = dThrottleMin + ((dThrottleMax - dThrottleMin)/(pitchMax  - pitchMin))*(mPitch - pitchMin);
                    throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
//                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
                    sThrottle = String.format("%.0f", throttleAvg);
                    mPitch = (dPitchMax - dPitchMin)/2 + dPitchMin;
                    pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
//                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
                    sPitch = String.format("%.0f", pitchAvg);
                }
                else { //pitch control
                    mPitch = dPitchMin + ((dPitchMax - dPitchMin)/(pitchMax - pitchMin))*(mPitch - pitchMin);
                    pitchAvg = pitchAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
//                    pitch.setText("pitch = " + String.format("%.0f", mPitch));
                    sPitch = String.format("%.0f", pitchAvg);
                    mPitch = (dThrottleMax - dThrottleMin)/2 + dThrottleMin;
                    throttleAvg = throttleAvg*(1.0 - 1.0/avgRange) + mPitch/avgRange; // calculate exponential average
//                    throttle.setText("throttle = " + String.format("%.0f", mPitch));
                    sThrottle = String.format("%.0f", throttleAvg);
                }
//                Log.d("sensors", "roll = " + mRoll);
                if ( mRoll > 100 || mRoll < 80){// roll control
                    if (mRoll > rollMax)
                        mRoll = rollMax;
                    else if (mRoll < rollMin)
                        mRoll = rollMin;
//                    Log.d("sensors", "outter");
                    mRoll = dRollMin + ((dRollMax - dRollMin)/(rollMax - rollMin))*(mRoll - rollMin);
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + mRoll/avgRange; // calculate exponential average
//                    roll.setText("roll = " + String.format("%.0f", mRoll));
                    sRoll = String.format("%.0f", rollAvg);
                }else {
//                    Log.d("sensors", "inner");
                    mRoll = (dRollMax - dRollMin)/2 + dRollMin;
                    rollAvg = rollAvg*(1.0 - 1.0/avgRange) + mRoll/avgRange; // calculate exponential average
//                    roll.setText("roll = " + String.format("%.0f", mRoll));
                    sRoll = String.format("%.0f", rollAvg);
                }
                //yaw control
                mAzimuth = dYawMin + ((dYawMax - dYawMin)/(yawMax - yawMin))*(mAzimuth - yawMin);
//                yaw.setText("yaw = " + String.format("%.0f", mAzimuth));
                yawAvg = yawAvg*(1.0 - 1.0/avgRange) + mAzimuth/avgRange; // calculate exponential average
//                yawAvg = ((dYawMax - dYawMin)/2+dYawMin)*(1.0 - 1.0/avgRange) + mAzimuth/avgRange; // calculate exponential average
                sYaw = String.format("%.0f", yawAvg);
                sensorData = (sRoll + "," + sPitch + "," + sThrottle + "," + sYaw);
//                sensorVals.setText(sensorData);
//                Log.d("sensors", sensorData);
                setSensorData(sensorData);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setSensorData(final String data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView)findViewById(R.id.sensorData);
                t.setText(data);
            }
        });
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String dataToSend;
        Socket s;

        MyClientTask(String addr, int port, String msg, Socket sock) {
            dstAddress = addr;
            dstPort = port;
            dataToSend = msg;
            s = sock;

        }

        @Override
        protected Void doInBackground(Void... arg0) {

//            Socket socket = null;

            try {
                // Socket Code!!!
//                socket = new Socket(dstAddress, dstPort);
                //loop through this
                dataToSend = getDataToSend();
//                while (dataToSend != "close") {
                if (socket == null || socket.isClosed()) {
                    try {
                        socket = new Socket(dstAddress, dstPort); //make new socket if first pass
                    } catch (ConnectException e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            public void run(){
                                Toast.makeText(getApplicationContext(), "Cool your jets!\nFalcon is not ready", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
                else if (!socket.isConnected()){
                    socket.connect(sockAddr); // reconnect if connection is lost
                } else {
                    Log.d("myClientTask", "Socket=" + (s == null ? "null" : ("" + s.toString())));
                    if (dataToSend != null && s != null) {
                        Log.d("sending", "About to send data");
                        if (dataToSend != "close") {
                            // add in a beginning of transmission symbol. The "=" symbol is arbitrary, but must
                            // be the same on the raspberry pi
                            dataToSend = "=" + dataToSend + "\n";
                            Log.d("dataSent", dataToSend);
                            OutputStream toServer = s.getOutputStream();
                            DataOutputStream out = new DataOutputStream(toServer);
//                    out.writeUTF("Hello from " + socket.getLocalSocketAddress());
                            out.writeUTF(dataToSend + "\n");
//                        Thread.sleep(105);
//                    InputStream fromServer = socket.getInputStream();
//                    DataInputStream in = new DataInputStream(fromServer);
//                    response = in.readUTF();
//                    dataToSend = getDataToSend();
                        } else {
                            s.close();
                        }
                    }
                }
//                }

//                socket.close();
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
            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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
            cleanup();
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
            cleanup();
            Intent intent = new Intent(SocketClient.this, Picam.class);
            String IpAddr = "@string/IPAddress";
            intent.putExtra("IP", IpAddr);
            startActivity(intent);

        } else if (id == R.id.nav_ipsettings) {
            // Handle the ip settings activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_websocket) {
            // Handle the web socket activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, Websocket.class);
            startActivity(intent);

        } else if (id == R.id.nav_bluetooth) {
            // Handle the web socket activity
            cleanup();
            Intent intent = new Intent(SocketClient.this, BluetoothLE.class);
            startActivity(intent);

        } else if (id == R.id.nav_home) {
            cleanup();
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
        if (stop == true){
            timer.cancel();
        } else {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (stop == true) {
//                        mWebView.loadUrl("https://s-media-cache-ak0.pinimg.com/736x/a2/57/90/a2579035dbf671b787a302aa816477a3.jpg");
                        this.cancel();
                    } else {
                        try {
                            MyClientTask myClientTask = new MyClientTask(IpAddress,
                                    Integer.parseInt(port),
                                    getDataToSend(),
                                    socket);
                            if (myClientTask.getStatus() == AsyncTask.Status.RUNNING) {
                                myClientTask.cancel(true);
                            } else {
                                myClientTask.execute();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 100);
        }
    }



//    private void LoadImageRepeatedly() {
//        Timer t = new Timer();
//        if (stop == true){
//            t.cancel();
//        } else {
//            t.scheduleAtFixedRate(new TimerTask() {
//                //            String x = "", y = "";
////            int i = 0, j = 0;
//                @Override
//                public void run() {
//                    if (stop == true){
//                        new LoadImage()
//                                .execute("https://s-media-cache-ak0.pinimg.com/736x/a2/57/90/a2579035dbf671b787a302aa816477a3.jpg");
//                        this.cancel();
//                    } else {
////                        x = "" + (300 + i++%7);
////                        y = "" + (200 + j++%13);
////
////                        new LoadImage().execute("https://unsplash.it/"+x+"/"+y);
////                        Log.d("image", "http://" + IpAddress + "/picam/cam.jpg");
//                        try {
//                            new LoadImage().execute("http://" + IpAddress + "/picam/cam.jpg");
//                            MyClientTask myClientTask = new MyClientTask(IpAddress,
//                                    Integer.parseInt(port),
//                                    getDataToSend(),
//                                    socket
//                            );
//                            if (myClientTask.getStatus() == AsyncTask.Status.RUNNING) {
//                                myClientTask.cancel(true);
//                                Log.d("Timer", "myClientTask is Running, has been cancelled");
//                            } else {
//                                myClientTask.execute();
//                                Log.d("Timer", "myClientTask has been executed");
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
////                        if (myClientTask.getStatus() == AsyncTask.Status.RUNNING){
////                            myClientTask.cancel(true);
////                        }
////                        else {
////                            myClientTask.execute();
////                        }
//                    }
//                }
//            }, 0, 105);
//        }
//    }

    private boolean cleanup (){
        //close socket
        //stop background tasks
        stop = true;
        try {
            mSensorManager.unregisterListener(this, mAcc);
            mSensorManager.unregisterListener(this, mMag);
        } catch (Exception e){
            e.printStackTrace();
            finish();
            return false;
        }
        try {
            socket.close();
//        } catch (Exception e){
//            e.printStackTrace();
//            return false;
        } catch (ConnectException e){
            e.printStackTrace();
            finish();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            finish();
            return false;
        }
//        try {
//            mWebView.setVisibility(View.GONE);
//        } catch (Exception e) {
//            e.printStackTrace();
//            finish();
//            return false;
//        }
        return true;

    }

    private boolean startup(){
        try{
            stop = false;
            mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
//            mWebView.setVisibility(View.VISIBLE);
//            LoadImageRepeatedly();
            MyClientTaskRepeated();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
