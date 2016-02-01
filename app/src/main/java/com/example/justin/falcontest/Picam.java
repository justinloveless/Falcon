package com.example.justin.falcontest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class Picam extends AppCompatActivity {
    SharedPreferences mPrefs;
    String IpAddress;

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picam);

        Intent intent = getIntent();
        String message = intent.getStringExtra("IP");
        TextView debugText = (TextView) findViewById(R.id.PicamIntentText);
        debugText.setText(message + "/picam");

        //Shared variables
        mPrefs = getSharedPreferences("SETTINGS", 0);
        IpAddress = "http://" + mPrefs.getString("IP", "") + "/picam"; //format as URL

        mWebView = (WebView) findViewById(R.id.activity_picam_webview);
        //Enable Javascript
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        //force links and redirects to open in web view instead of browser
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(IpAddress);

    }
}
