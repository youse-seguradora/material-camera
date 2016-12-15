package com.afollestad.materialcamera.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialcamera.util.Degrees;
import com.afollestad.materialcamera.util.ManufacturerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_BACK;
import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_FRONT;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class StillshotCameraFragment extends BaseStillshotCameraFragment implements View.OnClickListener {

    private static final String TAG = StillshotCameraFragment.class.getSimpleName();
    private static int MAX_ATTEMPTS_TO_AUTOFOCUS = 2;
    private int numberOfAttemptsToAutofocus = 0;
    CameraPreview mPreviewView;
    RelativeLayout mPreviewFrame;
    List<Integer> mFlashModes;
    private Camera mCamera;
    private Point mWindowSize;
    private boolean mIsAutoFocusing;

    public static StillshotCameraFragment newInstance() {
        return new StillshotCameraFragment();
    }

    private static Camera.Size chooseVideoSize(BaseStillshotCaptureInterface ci, List<Camera.Size> choices) {
        Camera.Size backupSize = null;
        for (Camera.Size size : choices) {
            if (size.height <= ci.videoPreferredHeight()) {
                if (size.width == size.height * ci.videoPreferredAspect())
                    return size;
                if (ci.videoPreferredHeight() >= size.height)
                    backupSize = size;
            }
        }
        if (backupSize != null) {
            return backupSize;
        }
        Log.d(TAG, "Couldn't find any suitable video size");
        return choices.get(choices.size() - 1);
    }

    private static Camera.Size chooseOptimalSize(List<Camera.Size> choices, int width, int height, Camera.Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Camera.Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.width;
        int h = aspectRatio.height;
        for (Camera.Size option : choices) {
            if (option.height == width * h / w &&
                    option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.d(TAG, "Couldn't find any suitable preview size");
            return aspectRatio;
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewFrame = (RelativeLayout) view.findViewById(R.id.rootFrame);
        mPreviewFrame.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mPreviewView.getHolder().getSurface().release();
        } catch (Throwable ignored) {
        }
        mPreviewFrame = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onPause() {
        if (mCamera != null) {
            mCamera.lock();
        }
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rootFrame) {
            if (mCamera == null || mIsAutoFocusing) {
                return;
            }

            autoFocus();
        }
    }

    private void autoFocus() {
        try {
            mIsAutoFocusing = true;
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mIsAutoFocusing = false;

                    if (!success) {
                        Log.e(TAG, "Unable to autofocus");
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void openCamera() {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) return;
        try {
            final int mBackCameraId = mCaptureInterface.getBackCamera() != null ? (Integer) mCaptureInterface.getBackCamera() : -1;
            final int mFrontCameraId = mCaptureInterface.getFrontCamera() != null ? (Integer) mCaptureInterface.getFrontCamera() : -1;
            if (mBackCameraId == -1 || mFrontCameraId == -1) {
                int numberOfCameras = Camera.getNumberOfCameras();
                if (numberOfCameras == 0) {
                    throwError(new Exception("No cameras are available on this device."));
                    return;
                }

                for (int i = 0; i < numberOfCameras; i++) {
                    //noinspection ConstantConditions
                    if (mFrontCameraId != -1 && mBackCameraId != -1) break;
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mFrontCameraId == -1) {
                        mCaptureInterface.setFrontCamera(i);
                    } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && mBackCameraId == -1) {
                        mCaptureInterface.setBackCamera(i);
                    }
                }
            }

            mCaptureInterface.setCameraPosition(CAMERA_POSITION_BACK);

            if (mWindowSize == null)
                mWindowSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
            final int toOpen = getCurrentCameraId();
            mCamera = Camera.open(toOpen == -1 ? 0 : toOpen);
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
            if (videoSizes == null || videoSizes.size() == 0)
                videoSizes = parameters.getSupportedPreviewSizes();
            Camera.Size mVideoSize = chooseVideoSize((BaseStillshotCaptureInterface) getParentFragment(), videoSizes);
            Camera.Size previewSize = chooseOptimalSize(parameters.getSupportedPreviewSizes(),
                    mWindowSize.x, mWindowSize.y, mVideoSize);

            if (ManufacturerUtil.isSamsungGalaxyS3()) {
                parameters.setPreviewSize(ManufacturerUtil.SAMSUNG_S3_PREVIEW_WIDTH,
                        ManufacturerUtil.SAMSUNG_S3_PREVIEW_HEIGHT);
            } else {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    parameters.setRecordingHint(true);
            }

            Camera.Size mStillShotSize = getHighestSupportedStillShotSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(mStillShotSize.width, mStillShotSize.height);

            setCameraDisplayOrientation(parameters);
            mCamera.setParameters(parameters);

            // NOTE: onFlashModesLoaded should not be called while modifying camera parameters as
            //       the flash parameters set in setupFlashMode will then be overwritten
            mFlashModes = CameraUtil.getSupportedFlashModes(this.getActivity(), parameters);
            mCaptureInterface.setFlashModes(mFlashModes);
            onFlashModesLoaded();

            createPreview();
        } catch (IllegalStateException e) {
            throwError(new Exception("Cannot access the camera.", e));
        } catch (RuntimeException e2) {
            throwError(new Exception("Cannot access the camera, you may need to restart your device.", e2));
        }
    }

    private Camera.Size getHighestSupportedStillShotSize(List<Camera.Size> supportedPictureSizes) {
        Collections.sort(supportedPictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.height * lhs.width > rhs.height * rhs.width)
                    return -1;
                return 1;

            }
        });
        Camera.Size maxSize = supportedPictureSizes.get(0);
        Log.d("StillshotCameraFragment", "Using resolution: " + maxSize.width + "x" + maxSize.height);
        return maxSize;
    }

    @SuppressWarnings("WrongConstant")
    private void setCameraDisplayOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(getCurrentCameraId(), info);
        final int deviceOrientation = Degrees.getDisplayRotation(getActivity());
        int mDisplayOrientation = Degrees.getDisplayOrientation(
                info.orientation, deviceOrientation, info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        Log.d("StillshotCameraFragment", String.format("Orientations: Sensor = %d˚, Device = %d˚, Display = %d˚",
                info.orientation, deviceOrientation, mDisplayOrientation));

        int previewOrientation;
        int jpegOrientation;
        if (CameraUtil.isChromium()) {
            previewOrientation = 0;
            jpegOrientation = 0;
        } else {
            jpegOrientation = previewOrientation = mDisplayOrientation;

            if (Degrees.isPortrait(deviceOrientation) && getCurrentCameraPosition() == CAMERA_POSITION_FRONT)
                previewOrientation = Degrees.mirror(mDisplayOrientation);
        }

        parameters.setRotation(jpegOrientation);
        mCamera.setDisplayOrientation(previewOrientation);
    }

    private void createPreview() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (mWindowSize == null)
            mWindowSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(mWindowSize);
        mPreviewView = new CameraPreview(getActivity(), mCamera);
        if (mPreviewFrame.getChildCount() > 0 && mPreviewFrame.getChildAt(0) instanceof CameraPreview)
            mPreviewFrame.removeViewAt(0);
        mPreviewFrame.addView(mPreviewView, 0);
        mPreviewView.setAspectRatio(mWindowSize.x, mWindowSize.y);
    }

    public void closeCamera() {
        try {
            if (mCamera != null) {
                try {
                    mCamera.lock();
                } catch (Throwable ignored) {
                }
                mCamera.release();
                mCamera = null;
            }
        } catch (IllegalStateException e) {
            //TODO
        }
    }

    public void takeStillshot() {
        takeStillshotAfterAutofocus();
    }

    private void takeStillshotAfterAutofocus() {
        try {
            mIsAutoFocusing = true;
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (!success && numberOfAttemptsToAutofocus <= MAX_ATTEMPTS_TO_AUTOFOCUS) {
                        Log.e(TAG, "Unable to autofocus before taking stillshot");
                        numberOfAttemptsToAutofocus++;
                        takeStillshotAfterAutofocus();
                        return;
                    }

                    numberOfAttemptsToAutofocus = 0;
                    mIsAutoFocusing = false;
                    takePictureNow();
                }
            });
        } catch (Throwable t) {
            mCameraListener.onCameraError(t);
        }
    }

    private void takePictureNow() {
        try {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    camera.stopPreview();
                    camera.startPreview();
                    mCameraListener.onTakePictureSuccess(bytes);
                }
            });
        } catch (Throwable t) {
            mCameraListener.onTakePictureError(t);
        }
    }

    static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }
}