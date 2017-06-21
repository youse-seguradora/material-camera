package com.afollestad.materialcamera.stillshot;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.BaseStillshotCameraFragment;
import com.afollestad.materialcamera.internal.BaseStillshotCaptureInterface;
import com.afollestad.materialcamera.internal.StillshotCameraFragment;
import com.afollestad.materialcamera.util.CameraUtil;

import java.util.List;

import static com.afollestad.materialcamera.stillshot.CameraFacing.BACK;

public class MaterialCameraStillshotFragment extends Fragment implements BaseStillshotCaptureInterface, TakePictureProxy {

    public static final int CAMERA_POSITION_UNKNOWN = 0;
    public static final int CAMERA_POSITION_FRONT = 1;
    public static final int CAMERA_POSITION_BACK = 2;
    public static final int FLASH_MODE_OFF = 0;
    private StillshotCameraListener listener;
    private BaseStillshotCameraFragment cameraFragment;
    @BaseCaptureActivity.CameraPosition
    private int mCameraPosition = CAMERA_POSITION_UNKNOWN;
    @BaseCaptureActivity.FlashMode
    private int mFlashMode = FLASH_MODE_OFF;
    private Object mFrontCameraId;
    private Object mBackCameraId;
    private List<Integer> mFlashModes;
    private CameraFacing cameraFacing = BACK;

    public static MaterialCameraStillshotFragment newInstance() {
        return new MaterialCameraStillshotFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mcam_fragment_external_stillshot, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!CameraUtil.hasCamera(getActivity())) {
            listener.onCameraError(new CameraUnavailableException("No camera available on device " + Build.MODEL));
            return;
        }

        setupWindow();
        loadCameraFragment();
    }

    private void loadCameraFragment() {
        // After encountering problems related to Camera2 on several devices we decided to force Camera1 until we have an acceptable version supporting Camera2.
//        cameraFragment = CameraUtil.hasCamera2(getActivity(), true) ?
//                StillshotCamera2Fragment.newInstance() : StillshotCameraFragment.newInstance();

        cameraFragment = StillshotCameraFragment.newInstance();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.stillshot_camera_container, cameraFragment)
                .commit();
    }

    private void setupWindow() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.BLACK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final View view = window.getDecorView();
                int flags = view.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                view.setSystemUiVisibility(flags);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraFragment != null) {
            cameraFragment.cleanup();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (StillshotCameraListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void takePicture() {
        cameraFragment.takeStillshot();
    }

    @Override
    public void changeCameraFacing(@NonNull CameraFacing cameraFacing) {
        if (this.cameraFacing == cameraFacing) {
            return;
        }
        this.cameraFacing = cameraFacing;
        switch (cameraFacing) {
            case BACK:
                setCameraPosition(BaseCaptureActivity.CAMERA_POSITION_BACK);
                cameraFragment.openCamera(BaseCaptureActivity.CAMERA_POSITION_BACK);
                break;
            case FRONT:
                setCameraPosition(BaseCaptureActivity.CAMERA_POSITION_FRONT);
                cameraFragment.openCamera(BaseCaptureActivity.CAMERA_POSITION_FRONT);
                break;
        }
    }

    @Override
    public void setCameraPosition(@BaseCaptureActivity.CameraPosition int position) {
        mCameraPosition = position;
    }

    @Override
    @BaseCaptureActivity.CameraPosition
    public int getCurrentCameraPosition() {
        return mCameraPosition;
    }

    @Override
    public Object getCurrentCameraId() {
        if (getCurrentCameraPosition() == BaseCaptureActivity.CAMERA_POSITION_FRONT)
            return getFrontCamera();
        else return getBackCamera();
    }

    @Override
    public Object getFrontCamera() {
        return mFrontCameraId;
    }

    @Override
    public void setFrontCamera(Object id) {
        mFrontCameraId = id;
    }

    @Override
    public Object getBackCamera() {
        return mBackCameraId;
    }

    @Override
    public void setBackCamera(Object id) {
        mBackCameraId = id;
    }

    @Override
    @BaseCaptureActivity.FlashMode
    public int getFlashMode() {
        return mFlashMode;
    }

    @Override
    public void toggleFlashMode() {
        if (mFlashModes != null) {
            mFlashMode = mFlashModes.get((mFlashModes.indexOf(mFlashMode) + 1) % mFlashModes.size());
        }
    }

    @Override
    public void setFlashModes(List<Integer> modes) {
        mFlashModes = modes;
    }

    @Override
    public float videoPreferredAspect() {
        return 4f / 3f;
    }

    @Override
    public int videoPreferredHeight() {
        return 720;
    }
}
