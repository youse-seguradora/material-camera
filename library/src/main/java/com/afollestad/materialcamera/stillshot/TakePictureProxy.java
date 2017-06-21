package com.afollestad.materialcamera.stillshot;

import android.support.annotation.NonNull;

public interface TakePictureProxy {

    void takePicture();

    void changeCameraFacing(@NonNull final CameraFacing cameraFacing);
}