package com.afollestad.materialcamerasample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialcamera.stillshot.CameraFacing;
import com.afollestad.materialcamera.stillshot.MaterialCameraStillshotFragment;
import com.afollestad.materialcamera.stillshot.StillshotCameraListener;
import com.afollestad.materialcamera.stillshot.TakePictureProxy;

public class DemoEmbeddedCameraActivity extends AppCompatActivity implements StillshotCameraListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1337;

    private CameraFacing cameraFacing = CameraFacing.BACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embedded_camera);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            loadCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Platform bug: http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loadCamera();
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                }
            }
        }
    }

    private void loadCamera() {
        Fragment f = MaterialCameraStillshotFragment.newInstance();
        final TakePictureProxy takePictureProxy = (TakePictureProxy) f;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();

        findViewById(R.id.take_picture_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePictureProxy.takePicture();
                    }
                });

        findViewById(R.id.change_camera_facing_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cameraFacing == CameraFacing.FRONT) {
                            cameraFacing = CameraFacing.BACK;
                        } else {
                            cameraFacing = CameraFacing.FRONT;
                        }
                        takePictureProxy.changeCameraFacing(cameraFacing);
                    }
                });
    }

    @Override
    public void onCameraError(Throwable t) {
        Toast.makeText(this, "onCameraError: " + String.valueOf(t), Toast.LENGTH_LONG).show();
        Log.i("onCameraError", String.valueOf(t));
    }

    @Override
    public void onTakePictureSuccess(byte[] image) {
        Log.i("onTakePictureSuccess", "");
    }

    @Override
    public void onTakePictureError(Throwable t) {
        Toast.makeText(this, "onCameraError: " + String.valueOf(t), Toast.LENGTH_LONG).show();
        Log.i("onTakePictureError", String.valueOf(t));
    }
}