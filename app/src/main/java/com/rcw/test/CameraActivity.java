package com.rcw.test;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rcw.customcamerdemo.BitmapUtils;
import com.rcw.customcamerdemo.DisplayUtils;
import com.rcw.customcamerdemo.OverLayerTopView;
import com.rcw.customcamerdemo.R;
import com.rcw.customcamerdemo.SdcardUtils;


public class CameraActivity extends AppCompatActivity implements Camera.PictureCallback,
        SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView mCameraSurfaceView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Point mPoint;//像素点
    // 取景框图层
    private OverLayerTopView mOverLayerView;
    //矩形取景框
    private Rect mCenterRect;
    //拍照和照片显示
    private ImageView ivTakePhoto, ivPreview;
    //切换摄像头和闪光灯
    private ImageView ivSwitchCamera,ivFlashMode;
    private Button btnCancel;
    //是否拍照完成和前置摄像头
    private boolean isTake = false,isFrontCamera = false,isPreview = false;
    private String[] flashModes = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};
    private int[] modelResId = {R.drawable.ic_camera_top_bar_flash_auto_normal, R.drawable.ic_camera_top_bar_flash_on_normal, R.drawable.ic_camera_top_bar_flash_off_normal, R.drawable.ic_camera_top_bar_flash_torch_normal};
    int modelIndex = 0;

    // 缩小原图片大小
    public  BitmapFactory.Options opt;
    {
        opt = new BitmapFactory.Options();
        opt.inSampleSize = 2;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        //初始化
        init();
        //添加事件监听
        addListener();
    }

    private void init() {
        mCameraSurfaceView =  findViewById(R.id.cameraSurfaceView);
        ivTakePhoto =  findViewById(R.id.iv_take_photo);
        btnCancel =  findViewById(R.id.btn_cancel);
        ivPreview =  findViewById(R.id.iv_preview);
        mOverLayerView =  findViewById(R.id.over_layer_view);
        //ivSwitchCamera =  findViewById(R.id.iv_switch_camera);
        //ivFlashMode =  findViewById(R.id.iv_flash_mode);

        mPoint = DisplayUtils.getScreenMetrics(this);
        // 设置取景框的margin; 距 左 、上 、右、下的 距离 单位是dp
        mCenterRect = DisplayUtils.createCenterRect(this, new Rect(80,200,80,200));
        mOverLayerView.setCenterRect(mCenterRect);
        mHolder = mCameraSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void addListener() {
        ivSwitchCamera.setOnClickListener(this);
        ivFlashMode.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        ivPreview.setOnClickListener(this);
        ivTakePhoto.setOnClickListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                isPreview = false;
            }

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 当holder被回收时 释放硬件
        releaseCamera();

    }

    private void releaseCamera() {
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
            }
            mCamera.release();
            mCamera = null;
        }
        isPreview = false;
    }

    private void switchCamera() throws Exception {
        isFrontCamera = !isFrontCamera;
        releaseCamera();
        openCamera();
        initCamera();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void openCamera() {
        if (!isFrontCamera) {//后置
            mCamera = Camera.open();
        } else {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                {
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        isFrontCamera = true;
                    }
                }
            }
        }
    }

    /**
     * 初始化照相机
     */
    public void initCamera() {
        if (mCamera != null && !isPreview) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                // 设置闪光灯为自动 前置摄像头时 不能设置
                if (!isFrontCamera) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                }

                resetCameraSize(parameters);
                // 设置图片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                // 设置JPG照片的质量
                parameters.set("jpeg-quality", 100);
                // 通过SurfaceView显示取景画面
                mCamera.setPreviewDisplay(mHolder);
                // 开始预览
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
            isPreview = true;
        }

    }

    /**
     * 旋转相机和设置预览大小
     *
     * @param parameters
     */
    public void resetCameraSize(Camera.Parameters parameters) {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            // 设置预览图片大小 为设备长宽
            parameters.setPreviewSize(cameraSize.width, cameraSize.height);
        }
        sizeList = parameters.getSupportedPictureSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            for (Camera.Size size : sizeList) {
                if (size.width * size.height == mPoint.x * mPoint.y) {
                    cameraSize = size;
                    break;
                }
            }
            // 设置图片大小 为设备长宽
            parameters.setPictureSize(cameraSize.width, cameraSize.height);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_take_photo:
                // 拍照前 线对焦 对焦后 拍摄（适用于自动对焦）
                isTake = true;
//			mCamera.autoFocus(autoFocusCallback);
                // 手动对焦
                mCamera.takePicture(null, null, this);
                break;
            /*case R.id.iv_switch_camera:
                try {
                    switchCamera();
                } catch (Exception e) {
                    mCamera = null;
                    e.printStackTrace();
                }
                break;
            case R.id.iv_flash_mode:
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                    Toast.makeText(this,"设备没有闪光灯",Toast.LENGTH_LONG).show();
                    return;
                }
                modelIndex++;
                if (modelIndex >= flashModes.length) {
                    modelIndex = 0;
                }
                android.hardware.Camera.Parameters parameters = mCamera.getParameters();
                List<String> flashModes = parameters.getSupportedFlashModes();
                if (flashModes.contains(this.flashModes[modelIndex])) {
                    parameters.setFlashMode(this.flashModes[modelIndex]);
                    ivFlashMode.setImageResource(modelResId[modelIndex]);
                }
                mCamera.setParameters(parameters);
                break;*/
            case R.id.btn_cancel:
                //getIntent().putExtra("")
                finish();

            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        isTake = false;
        if (data==null) return;
        // 拍照回掉回来的 图片数据。
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(data, 0, data.length, opt);
        Bitmap bm;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.setRotate(90, 0.1f, 0.1f);
            bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            if (isFrontCamera) {
                //前置摄像头旋转图片270度。
                matrix.setRotate(270);
                bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            }
        } else {
            bm = bitmap;
        }

        if (mCenterRect != null) {
            bitmap = BitmapUtils.getRectBitmap(mCenterRect, bm, mPoint);
        }
        //保存图片到sd卡
        if (SdcardUtils.existSdcard()){
            SdcardUtils.saveBitmap2SD(bitmap,"411524",System.currentTimeMillis()+"");
            //Toast.makeText(CameraActivity.this,"保存已经至"+SdcardUtils.+"目录文件夹下", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(CameraActivity.this,"未检测到SD卡", Toast.LENGTH_SHORT).show();
        //saveBitmapToSD(bitmap);

        BitmapUtils.recycleBitmap(bm);
        ivPreview.setImageBitmap(bitmap);
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.startPreview();
            isPreview = true;
        }
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (isTake) {
                // 点击拍照按钮 对焦 后 拍照
                // 第一个参数 是拍照的声音，未压缩的数据，压缩后的数据
                mCamera.takePicture(null, null, CameraActivity.this);
            }
        }
    };



}
