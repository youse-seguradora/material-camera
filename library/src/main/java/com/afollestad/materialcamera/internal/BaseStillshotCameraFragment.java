package com.afollestad.materialcamera.internal;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.stillshot.StillshotCameraListener;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseStillshotCameraFragment extends Fragment {

    protected BaseStillshotCaptureInterface mCaptureInterface;

    protected StillshotCameraListener mCameraListener;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mcam_fragment_base_stillshot, container, false);
    }

    protected void onFlashModesLoaded() {
        if (getCurrentCameraPosition() != BaseCaptureActivity.CAMERA_POSITION_FRONT) {
            invalidateFlash(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCaptureInterface = (BaseStillshotCaptureInterface) getParentFragment();

        if (context instanceof StillshotCameraListener) {
            mCameraListener = (StillshotCameraListener) context;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public abstract void openCamera();

    public abstract void closeCamera();

    public void cleanup() {
        closeCamera();
    }

    public abstract void takeStillshot();

    @Override
    public void onPause() {
        super.onPause();
        cleanup();
    }

    @Override
    public final void onDetach() {
        super.onDetach();
        mCaptureInterface = null;
        mCameraListener = null;
    }

    @BaseCaptureActivity.CameraPosition
    public final int getCurrentCameraPosition() {
        if (mCaptureInterface == null) return BaseCaptureActivity.CAMERA_POSITION_UNKNOWN;
        return mCaptureInterface.getCurrentCameraPosition();
    }

    public final int getCurrentCameraId() {
        if (mCaptureInterface.getCurrentCameraPosition() == BaseCaptureActivity.CAMERA_POSITION_BACK)
            return (Integer) mCaptureInterface.getBackCamera();
        else return (Integer) mCaptureInterface.getFrontCamera();
    }

    protected final void throwError(Exception e) {
        mCameraListener.onCameraError(e);
    }

    private void invalidateFlash(boolean toggle) {
        if (toggle) {
            mCaptureInterface.toggleFlashMode();
        }
    }
}