package com.rcw.customcamerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

import com.rcw.customcamerdemo.DisplayUtils;

/**
 * Created by ruancw on 2018/4/3.
 * 相机矩形取景框设置
 */

public class OverLayerTopView extends AppCompatImageView {
    //取景框四周的线的画笔
    private Paint mRectBorderPaint;
    //阴影部分的画笔
    private Paint mShadePaint;
    //矩形取景框四角的短线的画笔
    private Paint mLinePaint;
    //文字的画笔
    private Paint wordPaint;
    //中间矩形区域画板
    private Rect mCenterRect;
    //屏幕的宽和高
    private int screenWidth,screenHeight;

    private static final String TIPS = "请对准上半身进行拍照";

    public OverLayerTopView(Context context) {
        this(context,null,0);
    }

    public OverLayerTopView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public OverLayerTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化控件及参数
     * @param context 全局常量
     */
    private void init(Context context) {
        initPaint();
        Point mPoint= DisplayUtils.getScreenMetrics(context);
        screenWidth=mPoint.x;
        screenHeight=mPoint.y;
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        //中间矩形取景框的边界
        mRectBorderPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectBorderPaint.setColor(Color.RED);
        mRectBorderPaint.setStyle(Paint.Style.STROKE);
        mRectBorderPaint.setStrokeWidth(5f);
        mRectBorderPaint.setAlpha(0);//透明度

        //阴影区域的画笔
        mShadePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadePaint.setColor(Color.GRAY);
        mShadePaint.setStyle(Paint.Style.FILL);
        mShadePaint.setAlpha(100);

        //矩形四角的短线
        mLinePaint=new Paint();
        mLinePaint.setColor(Color.RED);
        mLinePaint.setAlpha(150);

        //顶部文字提示信息
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        wordPaint.setColor(Color.WHITE);//字体颜色
        wordPaint.setTextAlign(Paint.Align.CENTER);//居中显示
        wordPaint.setStrokeWidth(3f);//画笔的宽度
        wordPaint.setTextSize(45);//字体大小

    }

    /**
     * 设置取景框的矩形区域大小
     * @param mCenterRect 取景框矩形
     */
    public void setCenterRect(Rect mCenterRect){
         this.mCenterRect=mCenterRect;
         //postInvalidate();
    }

    /**
     * 取消中间取景框
     * @param mCenterRect 取景框
     */
    public void setDismissCenterRect(Rect mCenterRect){
        this.mCenterRect=null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //判断取景框矩形是否为空
        if (mCenterRect==null) return;
        //绘制阴影区域
        drawShadeRect(canvas);
        //绘制四角的短线
        drawLine(canvas);
        //绘制中间矩形取景框
        canvas.drawRect(mCenterRect,mRectBorderPaint);

        drawTipText(canvas);

        super.onDraw(canvas);
    }

    /**
     * 画文字提示
     * @param canvas 画布
     */
    private void drawTipText(Canvas canvas) {
        canvas.drawText(TIPS, mCenterRect.centerX(), mCenterRect.top-50, wordPaint);
    }

    /**
     * 绘制阴影区
     * @param canvas 画布
     */
    private void drawShadeRect(Canvas canvas) {
        canvas.drawRect(0,0,screenWidth,mCenterRect.top-2,mShadePaint);//顶部
        canvas.drawRect(0,mCenterRect.bottom+2,screenWidth,screenHeight,mShadePaint);//左侧
        canvas.drawRect(0,mCenterRect.top-2,mCenterRect.left-2,mCenterRect.bottom+2,mShadePaint);//下部
        canvas.drawRect(mCenterRect.right+2,mCenterRect.top-2,screenWidth,mCenterRect.bottom+2,mShadePaint);//右侧
    }

    /**
     * 绘制取景框四个角的短线
     */
    private void drawLine(Canvas canvas) {
        //左下
        canvas.drawRect(mCenterRect.left-2,mCenterRect.bottom,mCenterRect.left+50,mCenterRect.bottom+2,mLinePaint);//底部
        canvas.drawRect(mCenterRect.left-2,mCenterRect.bottom-50,mCenterRect.left,mCenterRect.bottom,mLinePaint);//左侧
        //左上
        canvas.drawRect(mCenterRect.left-2,mCenterRect.top-2,mCenterRect.left+50,mCenterRect.top,mLinePaint);//顶部
        canvas.drawRect(mCenterRect.left-2,mCenterRect.top,mCenterRect.left,mCenterRect.top+50,mLinePaint);//左侧
        //右上
        canvas.drawRect(mCenterRect.right-50,mCenterRect.top-2,mCenterRect.right+2,mCenterRect.top,mLinePaint);//顶部
        canvas.drawRect(mCenterRect.right,mCenterRect.top,mCenterRect.right+2,mCenterRect.top+50,mLinePaint);//右侧
        //右下
        canvas.drawRect(mCenterRect.right-50,mCenterRect.bottom,mCenterRect.right+2,mCenterRect.bottom+2,mLinePaint);//右侧
        canvas.drawRect(mCenterRect.right,mCenterRect.bottom-50,mCenterRect.right+2,mCenterRect.bottom,mLinePaint);//底部
    }

}
