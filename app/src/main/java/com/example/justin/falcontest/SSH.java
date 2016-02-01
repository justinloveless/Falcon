package com.example.justin.falcontest;

/**
 * Created by justin on 2/1/2016.
 */
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.app.Activity;

public class SSH extends Activity{


    public static String executeRemoteCmd (
            String username,
            String password,
            String hostname,
            int port,
            String command) throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        //Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        //SSH Channel
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        //execute command
        channelssh.setCommand(command);
        channelssh.connect();
        channelssh.disconnect();

        return baos.toString();
    }


}
