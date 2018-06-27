package com.rcw.customcamerdemo;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by ruancw on 2018/4/3.
 *
 */

public class BitmapUtils {

    /**
     * 获取矩形取景框的图片
     * @param mCenterRect 矩形取景框
     * @param bitmap 原图
     * @param point 显示的图像像素点
     * @return 返回截取的矩形框图片
     */
    public static Bitmap getRectBitmap(Rect mCenterRect, Bitmap bitmap, Point point){
        int width=mCenterRect.right-mCenterRect.left;
        int height=mCenterRect.bottom-mCenterRect.top;
        //Bitmap scaleBitmap=scaleBitmap(bitmap,point.x,point.y);
        Bitmap scaleBitmap=Bitmap.createScaledBitmap(bitmap,point.x,point.y,false);
        Bitmap rectBitmap=Bitmap.createBitmap(scaleBitmap,mCenterRect.left,mCenterRect.top,width,height);
        //Bitmap rectBitmap=Bitmap.createBitmap(bitmap,mCenterRect.left,mCenterRect.top,width,height);

        return rectBitmap;
    }

    /**
     * 缩放图片
     * @param bitmap 需要缩放的原图
     * @param w 宽度
     * @param h 高度
     * @return 返回缩放的图片
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        Bitmap bitmapOpt=bitmap;
        //获取图片原尺寸
        int width=bitmapOpt.getWidth();
        int height=bitmapOpt.getHeight();
        //图像像素点坐标
        int pWidth=w;
        int pHeight=h;
        //设置缩放的比例
        float scaleWidth=((float) pWidth)/width;
        float scaleHeight=((float) pHeight)/height;
        //通过矩阵缩放图片
        Matrix matrix=new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        //创建缩放后的图片
        Bitmap scaleBitmap=Bitmap.createBitmap(bitmapOpt,0,0,width,height,matrix,true);
        //释放资源
        bitmapOpt.recycle();

        return scaleBitmap;
    }

    /**
     * 回收图片资源
     * @param bitmap 图片资源
     */
    public static void recycleBitmap(Bitmap bitmap){
        if (!bitmap.isRecycled()){
            bitmap.recycle();
        }
    }


}
