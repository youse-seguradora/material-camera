package com.afollestad.materialcamera.stillshot;

public interface StillshotCameraListener {

    void onCameraError(Exception e);

    void onTakePictureSuccess(final byte[] image);

    void onTakePictureError(Exception e);
}