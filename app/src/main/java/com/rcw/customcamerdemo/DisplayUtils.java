package com.rcw.customcamerdemo;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by ruancw on 2018/4/2.
 */

public class DisplayUtils {

    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        Log.i("RCW", "Width = " + screenWidth + " Height = " + screenHeight
                + " densityDpi = " + dm.densityDpi);
        return new Point(screenWidth, screenHeight);

    }

    public static Rect createCenterRect(Context context, Rect rect) {
        int left = dp2px(context, rect.left);
        int top = dp2px(context, rect.top);
        int right = getScreenMetrics(context).x - dp2px(context, rect.right);
        int bottom = getScreenMetrics(context).y - dp2px(context, rect.bottom);
        Log.i("RCW", left + "@" + top + "@" + right + "@" + bottom);
        return new Rect(left, top, right, bottom);
    }

    /**
     * 将px装换成dp，保证尺寸不变
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue){
        float density = context.getResources().getDisplayMetrics().density;//得到设备的密度
        return (int) (pxValue/density+0.5f);
    }
    public static int dp2px(Context context, float dpValue){
        float density = context.getResources().getDisplayMetrics().density;
        Log.i("RCW", "density="+density);
        return (int) (dpValue*density+0.5f);
    }
    public static int px2sp(Context context, float pxValue){
        float scaleDensity = context.getResources().getDisplayMetrics().scaledDensity;//缩放密度
        return (int) (pxValue/scaleDensity+0.5f);
    }
    public static int sp2px(Context context, float spValue) {
        float scaleDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*scaleDensity+0.5f);
    }
}
