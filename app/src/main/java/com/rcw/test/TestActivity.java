package com.rcw.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rcw.customcamerdemo.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestActivity extends AppCompatActivity {

    private MyView myView;
    private View view2, view3, view4;//3个蒙板
    private TextView view1;//1个蒙板
    private Camera camera;
    private Button takepicture_button;
    private SurfaceView surfaceview;
    private int screenWidth, screenHeight;
    private int myViewPaddingLeft = 500, myViewPaddingTop = 300;//MyView上下距离屏幕的距离（意思是MyView上下两边一共空了500）

    private ToneGenerator tone;
    private Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.w("print", "聚焦完成，，，，"); //聚焦后发出提示音
            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//把屏幕设置成横屏
        setContentView(R.layout.activity_test);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        view1 = (TextView) findViewById(R.id.view1);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) view1.getLayoutParams();
        layoutParams1.height = myViewPaddingTop / 2;
        view1.setLayoutParams(layoutParams1);
        myView = (MyView) findViewById(R.id.myView);
        myView.setMyParams(screenWidth, screenHeight, myViewPaddingLeft, myViewPaddingTop);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) myView.getLayoutParams();
        layoutParams.width = screenWidth - myViewPaddingLeft;
        layoutParams.height = screenHeight - myViewPaddingTop;
        myView.setLayoutParams(layoutParams);
        view2 = findViewById(R.id.view2);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) view2.getLayoutParams();
        layoutParams2.width = myViewPaddingLeft / 2;
        layoutParams2.height = screenHeight - myViewPaddingTop;
        view2.setLayoutParams(layoutParams2);
        view3 = findViewById(R.id.view3);
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) view3.getLayoutParams();
        layoutParams3.width = myViewPaddingLeft / 2;
        layoutParams3.height = screenHeight - myViewPaddingTop;
        view3.setLayoutParams(layoutParams3);
        view4 = findViewById(R.id.view4);
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) view4.getLayoutParams();
        layoutParams4.height = myViewPaddingTop / 2;
        view4.setLayoutParams(layoutParams4);
        surfaceview = (SurfaceView) findViewById(R.id.surfaceView);

        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                camera.autoFocus(myAutoFocusCallback);
                return false;
            }
        });
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SurfaceHolder holder = surfaceview.getHolder();
        holder.setKeepScreenOn(true);
        // 屏幕常亮
        holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        holder.addCallback(new MySurfaceCallback());
        holder.lockCanvas();
    }

    private final class MySurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //当surface的格式或大小发生改变，这个方法就被调用
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                camera.setPreviewDisplay(surfaceview.getHolder());
                // 开启预览
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    public void takepicture(View v) {
        camera.takePicture(mShutterCallback, null, mPictureCallback);
    }

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap mBitmapCut = ImageCrop(mBitmap);
            String rootPath = Environment.getExternalStorageDirectory()
                    + "/pajx/stu_photo" + File.separator;
            File file = new File(rootPath + System.currentTimeMillis() + ".jpg");
            try {
                file.createNewFile();
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                mBitmapCut.compress(Bitmap.CompressFormat.JPEG, 100, os);//100 是压缩率,100表示不压缩
                os.flush();
                os.close();
                Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 按图片比例裁切图片
     */
    public Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth();
        // 得到图片的宽，高
        int h = bitmap.getHeight();
        //剪切的初始xy位置，原点位置 都是根据比例来算的
        int x = (int) ((w * (myViewPaddingLeft / 2)) / screenWidth);
        int y = (int) (h * (myViewPaddingTop / 2) / screenHeight);
        //剪切的宽和高
        int retX = (int) ((w * (screenWidth - myViewPaddingLeft)) / screenWidth);
        int retY = (int) (h * (screenHeight - myViewPaddingTop) / screenHeight);
        //必须x+retX要小于或等于bitmap.getWidth()，y+retY要小于或等于bitmap.getHeight()，不然会报错
        return Bitmap.createBitmap(bitmap, x, y, retX, retY, null, false);
    }

}
