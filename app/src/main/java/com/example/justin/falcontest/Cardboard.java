package com.example.justin.falcontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class Cardboard extends CardboardActivity implements CardboardView.StereoRenderer {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cardboard);
//        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
//        cardboardView.setRenderer(this);
//        cardboardView.setTransitionViewEnabled(true);
//        cardboardView.setOnCardboardBackButtonListener(new Runnable() {
//            @Override
//            public void run() {
//                onBackPressed();
//            }
//        });
//        setCardboardView(cardboardView);
//
//
//    }
//
    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {

    }

    @Override
    public void onRendererShutdown() {

    }
}
