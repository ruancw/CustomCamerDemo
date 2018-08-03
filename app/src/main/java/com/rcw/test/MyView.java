package com.rcw.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ruancw on 2018/4/23.
 */

public class MyView extends View {
    //获取屏幕的宽和高。根据屏幕的宽和高来算取景框的位置
    private int screenWidth, screenHeight, myViewPaddingLeft, myViewPaddingTop;
    private int  langLine = 100, //取景框长线的长度
            shortLine = 50, //取景框短线的长度
            origin = 0;//原点

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMyParams(int screenWidth, int screenHeight, int myViewPaddingLeft, int myViewPaddingTop) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.myViewPaddingLeft = myViewPaddingLeft;
        this.myViewPaddingTop = myViewPaddingTop;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAlpha(250);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        // 下面是取景框的8条线
        // xy的算法是：把屏幕横着(逆时针旋转90度的屏幕)，相对这个view来讲(左上角是原点0,0)，从左到右是x轴，从上到下是y轴
        //左上角两条线
        canvas.drawLine(origin, origin, langLine, origin, paint);
        canvas.drawLine(origin, origin, origin, shortLine, paint);
        //左下角两条线
        canvas.drawLine(origin, screenHeight - myViewPaddingTop, langLine, screenHeight - myViewPaddingTop, paint);
        canvas.drawLine(origin, screenHeight - myViewPaddingTop, origin, screenHeight - myViewPaddingTop - shortLine, paint);
        //右上角两条线
        canvas.drawLine(screenWidth - myViewPaddingLeft, origin, screenWidth - myViewPaddingLeft - langLine, origin, paint);
        canvas.drawLine(screenWidth - myViewPaddingLeft, origin, screenWidth - myViewPaddingLeft, shortLine, paint);
        //右下角两条线
        canvas.drawLine(screenWidth - myViewPaddingLeft, screenHeight - myViewPaddingTop, screenWidth - myViewPaddingLeft - langLine, screenHeight - myViewPaddingTop, paint);
        canvas.drawLine(screenWidth - myViewPaddingLeft, screenHeight - myViewPaddingTop, screenWidth - myViewPaddingLeft, screenHeight - myViewPaddingTop - shortLine, paint);
        super.onDraw(canvas);
    }
}
