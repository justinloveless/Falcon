package com.example.justin.falcontest;

/**
 * Author: justin
 * Created: 2/1/2016
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
//import com.jcraft.jsch.ChannelExec;
//import com.jcraft.jsch.ChannelShell;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class SSH extends Activity{
//public class SSH extends AsyncTask </*Params*/String, /*Progress*/Integer, /*Result*/String>
//    static Session session;
//    static ChannelShell channelssh;
    public static void connect (
            String username,
            String password,
            String hostname,
            int port) throws Exception{

//        JSch jsch = new JSch();
//        session = jsch.getSession(username,hostname, port);
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        prop.put("PrefferedAuthentications", "password");
//        session.setConfig(prop);
//        //Log.d("SSH", "post Properties");
//        session.setPort(22);
//        //Log.d("SSH", "pre setPassword");
//        session.setPassword(password);
        //Log.d("SSH", "post setPassword");

        //Log.d("preconnect", " something");
        //Log.d("dummy", "something");
        //Log.d("Host", session.getHost());

//        session.connect(10000); //connect with 5 second timeout
        //Log.d("postconnect", "something");


        //SSH Channel
        //channelssh = (ChannelExec) session.openChannel("exec");
        //channelssh.connect();

    }

    public static String executeRemoteCmd (String command) throws Exception {


        //SSH Channel
//        channelssh = (ChannelShell) session.openChannel("exec");

        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //channelssh.setOutputStream(baos);
        //Log.d("SSH", "Set Output Stream");

        OutputStream outputStream = null;
        byte[] b = command.getBytes();
        outputStream.write(b);
        //execute command
       // channelssh.setCommand(command);
//        channelssh.setOutputStream(outputStream);
//        channelssh.connect();
        outputStream.flush();
        //channelssh.run();
        //Log.d("SSH", "Command sent");
        //String baos_str = baos.toString();

        //Log.d("baos", "Baos = "+baos_str);
        //Log.d("boas", baos_str);

//        InputStream inputStream = channelssh.getInputStream();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        Log.d("loop", "about to start loop");
//        if (channelssh.isConnected()){
//            Log.d("connected", "is connected");
//        }
//        while ((line = bufferedReader.readLine()) != null){
//            //Log.d("loop", "still in loop, before string builder");
//            stringBuilder.append(line);
//            stringBuilder.append("\n");
//            //Log.d("loop", "still in loop, after string builder");
//        }

        Log.d("input", "input string = " + stringBuilder);

//        channelssh.disconnect();


        return stringBuilder.toString();
    }

    public static void disconnect(){
        //channelssh.disconnect();
//        session.disconnect();
    }

}
