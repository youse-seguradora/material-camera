package com.afollestad.materialcamera.stillshot;

public interface StillshotCameraListener {

    void onCameraRetry(Exception e);

    void onCameraError(Exception e);

    void onPictureTakenSuccess(final byte[] image);

    void onPictureTakenError(Exception e);

}