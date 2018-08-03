package com.rcw.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ruancw on 2018/5/7.
 */

public class MyCropView extends View {
    // Private Constants ///////////////////////////////////////////////////////
    private static final float BMP_LEFT = 0f;
    private static final float BMP_TOP = 20f;

    private static final float DEFAULT_BORDER_RECT_WIDTH = 200f;
    private static final float DEFAULT_BORDER_RECT_HEIGHT = 200f;

    // this constant would be best to use event number
    private static final float BORDER_LINE_WIDTH = 6f;
    private static final float BORDER_CORNER_LENGTH = 30f;
    private static final float TOUCH_FIELD = 10f;

    // Member Variables ////////////////////////////////////////////////////////
    private Bitmap mBitmap;
    private Bitmap mBmpToCrop;
    private RectF mBmpBound;
    private Paint mBmpPaint;

    private Paint mBorderPaint;// 裁剪区边框
    private Paint mCornerPaint;
    private Paint mBgPaint;

    private RectF mDefaultBorderBound;
    private RectF mBorderBound;

    private PointF mLastPoint = new PointF();

    private float mBorderWidth;
    private float mBorderHeight;

    private int touchPos;

    // Constructors ////////////////////////////////////////////////////////////
    public MyCropView(Context context) {
        super(context);
        init(context);
    }

    public MyCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // View Methods ////////////////////////////////////////////////////////////
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBmpToCrop, null, mBmpBound, mBmpPaint);
            canvas.drawRect(mBorderBound.left, mBorderBound.top, mBorderBound.right, mBorderBound.bottom, mBorderPaint);
            //drawBackground(canvas);
        }
    }


    // Public Methods //////////////////////////////////////////////////////////
    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
        setBmp();
    }

    public Bitmap getCroppedImage() {
        // 先不考虑图片被压缩的情况 就当作现在的图片就是1：1的

        return Bitmap.createBitmap(mBmpToCrop, (int) mBorderBound.left, (int) mBorderBound.top, (int) mBorderWidth,
                (int) mBorderHeight);
    }

    // Private Methods /////////////////////////////////////////////////////////
    private void init(Context context) {

        mBmpPaint = new Paint();
        // 以下是抗锯齿
        mBmpPaint.setAntiAlias(true);// 防止边缘的锯齿
        mBmpPaint.setFilterBitmap(true);// 对位图进行滤波处理

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(Color.parseColor("#AAFFFFFF"));
        mBorderPaint.setStrokeWidth(BORDER_LINE_WIDTH);

        mCornerPaint = new Paint();

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.parseColor("#B0000000"));
        mBgPaint.setAlpha(150);

    }

    private void setBmp() {
        mBmpToCrop = mBitmap;

        mBmpBound = new RectF();
        mBmpBound.left = BMP_LEFT;
        mBmpBound.top = BMP_TOP;
        mBmpBound.right = mBmpBound.left + mBmpToCrop.getWidth();
        mBmpBound.bottom = mBmpBound.top + mBmpToCrop.getHeight();

        // 使裁剪框一开始出现在图片的中心位置
        mDefaultBorderBound = new RectF();
        mDefaultBorderBound.left = (mBmpBound.left + mBmpBound.right - DEFAULT_BORDER_RECT_WIDTH) / 2;
        mDefaultBorderBound.top = (mBmpBound.top + mBmpBound.bottom - DEFAULT_BORDER_RECT_HEIGHT) / 2;
        mDefaultBorderBound.right = mDefaultBorderBound.left + DEFAULT_BORDER_RECT_WIDTH;
        mDefaultBorderBound.bottom = mDefaultBorderBound.top + DEFAULT_BORDER_RECT_HEIGHT;

        mBorderBound = new RectF();
        mBorderBound.left = mDefaultBorderBound.left;
        mBorderBound.top = mDefaultBorderBound.top;
        mBorderBound.right = mDefaultBorderBound.right;
        mBorderBound.bottom = mDefaultBorderBound.bottom;

        getBorderEdgeLength();
        invalidate();
    }

    private void drawBackground(Canvas canvas) {

                /*-
           -------------------------------------
           |                top                |
           -------------------------------------
          |      |                    |       |<——————————mBmpBound
           |      |                    |       |
          | left |                    | right |
           |      |                    |       |
           |      |                  <─┼───────┼────mBorderBound
          -------------------------------------
           |              bottom               |
           -------------------------------------
          */

        // Draw "top", "bottom", "left", then "right" quadrants.
        // because the border line width is larger than 1f, in order to draw a complete border rect ,
        // i have to change zhe rect coordinate to draw
        float delta = BORDER_LINE_WIDTH / 2;
        float left = mBorderBound.left - delta;
        float top = mBorderBound.top - delta;
        float right = mBorderBound.right + delta;
        float bottom = mBorderBound.bottom + delta;

        // -------------------------------------------------------------------------------移动到上下两端会多出来阴影
        canvas.drawRect(mBmpBound.left, mBmpBound.top, mBmpBound.right, top, mBgPaint);
        canvas.drawRect(mBmpBound.left, bottom, mBmpBound.right, mBmpBound.bottom, mBgPaint);
        canvas.drawRect(mBmpBound.left, top, left, bottom, mBgPaint);
        canvas.drawRect(right, top, mBmpBound.right, bottom, mBgPaint);
    }


    private void setLastPosition(MotionEvent event) {
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();
    }

    private void getBorderEdgeLength() {
        mBorderWidth = mBorderBound.width();
        mBorderHeight = mBorderBound.height();
    }

    private void getBorderEdgeWidth() {
        mBorderWidth = mBorderBound.width();
    }

    private void getBorderEdgeHeight() {
        mBorderHeight = mBorderBound.height();
    }

    private void resetLeft(float delta) {
        mBorderBound.left += delta;

        getBorderEdgeWidth();
        fixBorderLeft();
    }

    private void resetTop(float delta) {
        mBorderBound.top += delta;
        getBorderEdgeHeight();
        fixBorderTop();
    }

    private void resetRight(float delta) {
        mBorderBound.right += delta;

        getBorderEdgeWidth();
        fixBorderRight();

    }

    private void resetBottom(float delta) {
        mBorderBound.bottom += delta;

        getBorderEdgeHeight();
        fixBorderBottom();
    }

    private void fixBorderLeft() {
        // fix left
        if (mBorderBound.left < mBmpBound.left)
            mBorderBound.left = mBmpBound.left;
        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.left = mBorderBound.right - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderTop() {
        // fix top
        if (mBorderBound.top < mBmpBound.top)
            mBorderBound.top = mBmpBound.top;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.top = mBorderBound.bottom - 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderRight() {
        // fix right
        if (mBorderBound.right > mBmpBound.right)
            mBorderBound.right = mBmpBound.right;
        if (mBorderWidth < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.right = mBorderBound.left + 2 * BORDER_CORNER_LENGTH;
    }

    private void fixBorderBottom() {
        // fix bottom
        if (mBorderBound.bottom > mBmpBound.bottom)
            mBorderBound.bottom = mBmpBound.bottom;
        if (mBorderHeight < 2 * BORDER_CORNER_LENGTH)
            mBorderBound.bottom = mBorderBound.top + 2 * BORDER_CORNER_LENGTH;
    }
}
