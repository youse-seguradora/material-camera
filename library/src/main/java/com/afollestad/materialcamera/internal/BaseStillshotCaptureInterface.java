package com.afollestad.materialcamera.internal;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

public interface BaseStillshotCaptureInterface {

    void setCameraPosition(int position);

    Object getCurrentCameraId();

    @BaseCaptureActivity.CameraPosition
    int getCurrentCameraPosition();

    void setFrontCamera(Object id);

    void setBackCamera(Object id);

    Object getFrontCamera();

    Object getBackCamera();

    void toggleFlashMode();

    int videoPreferredHeight();

    float videoPreferredAspect();

    @BaseCaptureActivity.FlashMode
    int getFlashMode();

    void setFlashModes(List<Integer> modes);
}
