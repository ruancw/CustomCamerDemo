package com.rcw.customcamerdemo;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import id.zelory.compressor.Compressor;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = "RCW";

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
    private Button btnCancel;
    //是否拍照完成和前置摄像头
    private boolean isTake = false, isFrontCamera = false, isPreview = false;
    // 缩小原图片大小
    public BitmapFactory.Options opt;

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
        mCameraSurfaceView = findViewById(R.id.cameraSurfaceView);
        ivTakePhoto = findViewById(R.id.iv_take_photo);
        btnCancel = findViewById(R.id.btn_cancel);
        ivPreview = findViewById(R.id.iv_preview);
        mOverLayerView = findViewById(R.id.over_layer_view);

        mPoint = DisplayUtils.getScreenMetrics(this);
        // 设置取景框的margin; 距 左 、上 、右、下的 距离 单位是dp
        mCenterRect = DisplayUtils.createCenterRect(this, new Rect(80, 120, 80, 240));
        mOverLayerView.setCenterRect(mCenterRect);
        mHolder = mCameraSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void addListener() {
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

    /**
     * 释放相机资源
     */
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

    /**
     * 打开相机
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void openCamera() {
        if (!isFrontCamera) {//后置摄像头
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(mHolder);//摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            Camera.Parameters parameters = mCamera.getParameters();
            // 设置闪光灯为自动 前置摄像头时 不能设置
            if (!isFrontCamera) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            setCameraParams(mPoint.x, mPoint.y);
            mCamera.startPreview();
            isPreview = true;
            mCamera.cancelAutoFocus();//自动对焦。
        }

    }

    /**
     * 设置相机分辨率
     * @param width 宽度
     * @param height 高度
     */
    private void setCameraParams(int width, int height) {
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        //从列表中选取合适的分辨率
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height);
        mCameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }

        //mCamera.cancelAutoFocus();//自动对焦。
        // 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_take_photo:
                // 拍照前 线对焦 对焦后 拍摄（适用于自动对焦）
                isTake = true;
                // 手动对焦
                setCameraParams(mPoint.x,mPoint.y);
                mCamera.takePicture(null, null, jpeg);
                break;
            case R.id.btn_cancel:
                finish();
            default:
                break;
        }
    }
    private Bitmap compressedImage;
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            isTake = false;
            if (data == null) return;
            // 拍照回掉回来的 图片数据。
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
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
//            //图片缩放到341x481大小
//            scaleBitmap=Bitmap.createScaledBitmap(bitmap,mCenterRect.width(),mCenterRect.height(),false);
            if (mCenterRect != null) {
                //customCompressImage();
                //获取取景框内的图片
                bitmap = BitmapUtils.getRectBitmap(mCenterRect, bm, mPoint);
            }
//            //保存图片到sd卡
            if (SdcardUtils.existSdcard()) {
//                SdcardUtils.saveBitmap2SD(scaleBitmap, "411524", 100000002 + "");
                SdcardUtils.saveBitmap2SD(bitmap, "411524", System.currentTimeMillis() + "");

            } else Toast.makeText(CameraActivity.this, "未检测到SD卡", Toast.LENGTH_SHORT).show();
            //customCompressImage();
            BitmapUtils.recycleBitmap(bm);
            //显示预览图
            ivPreview.setImageBitmap(bitmap);
            //释放相机资源
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.startPreview();
                isPreview = true;
            }
        }
    };

    /**
     * Compress压缩图片
     */
    public void customCompressImage() {
            // Compress image in main thread using custom Compressor
            try {
                compressedImage = new Compressor(this)
                        .setMaxWidth(mPoint.x)
                        .setMaxHeight(mPoint.x)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStorageDirectory()
                                + "/pajx/stu_photo/411525/")
                        .compressToBitmap(SdcardUtils.getOutputMediaFile("411526","100000"));
                //Bitmap bitmap=BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                //SdcardUtils.saveBitmap2SD(bitmap, "411524", 100000002 + "");
            } catch (IOException e) {
                e.printStackTrace();
                //showError(e.getMessage());
            }


    }


}
