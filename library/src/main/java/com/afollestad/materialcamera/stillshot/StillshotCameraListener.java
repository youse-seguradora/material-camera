package com.afollestad.materialcamera.stillshot;

public interface StillshotCameraListener {

    void onCameraError(Throwable t);

    void onTakePictureSuccess(final byte[] image);

    void onTakePictureError(Throwable t);
}