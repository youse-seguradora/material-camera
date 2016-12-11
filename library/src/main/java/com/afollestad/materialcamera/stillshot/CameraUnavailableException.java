package com.afollestad.materialcamera.stillshot;

import android.support.annotation.NonNull;

public class CameraUnavailableException extends Exception {

    CameraUnavailableException(@NonNull final String message) {
        super(message);
    }
}
