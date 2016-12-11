package com.afollestad.materialcamerasample;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialcamera.stillshot.CameraControl;
import com.afollestad.materialcamera.stillshot.StillshotCameraListener;
import com.afollestad.materialcamera.stillshot.MaterialCameraStillshotFragment;

public class DemoEmbeddedCameraActivity extends AppCompatActivity implements StillshotCameraListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embedded_camera);
        Fragment f = MaterialCameraStillshotFragment.newInstance();
        final CameraControl control = (CameraControl) f;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();

        findViewById(R.id.take_picture_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        control.takePicture();
                    }
                });
    }

    @Override
    public void onCameraRetry(Exception e) {
        Log.i("onCameraRetry", String.valueOf(e));
    }

    @Override
    public void onCameraError(Exception e) {
        Log.i("onCameraError", String.valueOf(e));
    }

    @Override
    public void onPictureTakenSuccess(byte[] image) {
        Log.i("onPictureTakenSuccess", "");
    }

    @Override
    public void onPictureTakenError(Exception e) {
        Log.i("onPictureTakenError", String.valueOf(e));
    }
}