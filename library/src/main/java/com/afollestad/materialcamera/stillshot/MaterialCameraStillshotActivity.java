package com.afollestad.materialcamera.stillshot;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.internal.BaseStillshotCaptureInterface;
import com.afollestad.materialcamera.internal.CameraIntentKey;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class MaterialCameraStillshotActivity extends AppCompatActivity implements BaseStillshotCaptureInterface {

    public static final int CAMERA_POSITION_UNKNOWN = 0;
    public static final int CAMERA_POSITION_FRONT = 1;
    public static final int CAMERA_POSITION_BACK = 2;
    public static final int FLASH_MODE_OFF = 0;
    private int mCameraPosition = CAMERA_POSITION_UNKNOWN;
    private int mFlashMode = FLASH_MODE_OFF;
    private Object mFrontCameraId;
    private Object mBackCameraId;
    private List<Integer> mFlashModes;

    @Override
    protected final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("camera_position", mCameraPosition);
        if (mFrontCameraId instanceof String) {
            outState.putString("front_camera_id_str", (String) mFrontCameraId);
            outState.putString("back_camera_id_str", (String) mBackCameraId);
        } else {
            if (mFrontCameraId != null)
                outState.putInt("front_camera_id_int", (Integer) mFrontCameraId);
            if (mBackCameraId != null)
                outState.putInt("back_camera_id_int", (Integer) mBackCameraId);
        }
        outState.putInt("flash_mode", mFlashMode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);

        if (!CameraUtil.hasCamera(this)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.mcam_error)
                    .content(R.string.mcam_video_capture_unsupported)
                    .positiveText(android.R.string.ok)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
            return;
        }
        setContentView(R.layout.mcam_activity_videocapture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int primaryColor = getIntent().getIntExtra(CameraIntentKey.PRIMARY_COLOR, 0);
            final boolean isPrimaryDark = CameraUtil.isColorDark(primaryColor);
            final Window window = getWindow();
            window.setStatusBarColor(CameraUtil.darkenColor(primaryColor));
            window.setNavigationBarColor(isPrimaryDark ? primaryColor : Color.BLACK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final View view = window.getDecorView();
                int flags = view.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                view.setSystemUiVisibility(flags);
            }
        }

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getInt("camera_position", -1);
            if (savedInstanceState.containsKey("front_camera_id_str")) {
                mFrontCameraId = savedInstanceState.getString("front_camera_id_str");
                mBackCameraId = savedInstanceState.getString("back_camera_id_str");
            } else {
                mFrontCameraId = savedInstanceState.getInt("front_camera_id_int");
                mBackCameraId = savedInstanceState.getInt("back_camera_id_int");
            }
            mFlashMode = savedInstanceState.getInt("flash_mode");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected final void onPause() {
        super.onPause();
        if (!isFinishing() && !isChangingConfigurations())
            finish();
    }

    @Override
    public final void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.stillshot_camera_container);
        if (frag != null) {
            if (frag instanceof MaterialCameraStillshotFragment) {
//                ((MaterialCameraStillshotFragment) frag).cleanup();
            }
        }
        finish();
    }

    @Override
    public void setCameraPosition(int position) {
        mCameraPosition = position;
    }

    @Override
    public int getCurrentCameraPosition() {
        return mCameraPosition;
    }

    @Override
    public Object getCurrentCameraId() {
        if (getCurrentCameraPosition() == CAMERA_POSITION_FRONT)
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
        return getIntent().getFloatExtra(CameraIntentKey.VIDEO_PREFERRED_ASPECT, 4f / 3f);
    }

    @Override
    public int videoPreferredHeight() {
        return getIntent().getIntExtra(CameraIntentKey.VIDEO_PREFERRED_HEIGHT, 720);
    }

}