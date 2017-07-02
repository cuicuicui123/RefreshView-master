package com.cwc.refreshview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.security.cert.PolicyNode;

/**
 * @author Cuiweicong
 */

public class MyRefreshView extends View {
    private int mLocalHeight;
    private int mHeight;
    private int mWidth;
    private int mMaxHeight;
    private int mDragHeight;
    private Paint mPaint;
    private Path mPath;

    private int multiple = 3;

    private boolean mIsReseting = false;

    public MyRefreshView(Context context) {
        this(context, null);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mMaxHeight = (int) getResources().getDimension(R.dimen.height);
        mLocalHeight = mHeight = mMaxHeight;
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.blue));
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mHeight < mMaxHeight ? mHeight : mMaxHeight;
        PointF start = new PointF(0, height);
        PointF end = new PointF(mWidth, height);
        PointF control = new PointF(mWidth / 2, mHeight);

        PointF highest = getBezierPointF((float) 0.5, start, end, control);
        int measureHeight = (int) highest.y;
        setMeasuredDimension(mWidth, measureHeight);
    }

    /**
     * 计算 二阶贝塞尔曲线的坐标
     *
     * @param t             曲线长度比例， 进度比例 [0, 1]
     * @param startPointF   开始点
     * @param controlPointF 控制点
     * @param endPointF     结束点
     * @return
     */
    public static PointF getBezierPointF(float t, PointF startPointF, PointF endPointF, PointF controlPointF) {
        PointF pointF = new PointF(0, 0);
        float tmp = 1 - t;
        pointF.x = tmp * tmp * startPointF.x + 2 * t * tmp * controlPointF.x + t * t * endPointF.x;
        pointF.y = tmp * tmp * startPointF.y + 2 * t * tmp * controlPointF.y + t * t * endPointF.y;
        return pointF;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, mWidth, mLocalHeight, mPaint);
        mPath.reset();
        mPath.moveTo(0, mLocalHeight);
        mPath.quadTo(mWidth / 2, mHeight, mWidth, mLocalHeight);
        canvas.drawPath(mPath, mPaint);
        if (mIsReseting){
            resetView();
            
        }
    }

    public void addHeight(int height){
        mHeight = mLocalHeight;
        mDragHeight = height / multiple;
        mHeight += mDragHeight;
        requestLayout();
    }

    public void reset(){
        mIsReseting = true;
        invalidate();
    }

    public void resetView(){
        //这里有问题
        if (mHeight > (mMaxHeight + 10)) {
            mHeight -= (mHeight - mMaxHeight) / 10;
            requestLayout();
        } else {
            mHeight = mMaxHeight;
            mLocalHeight = mMaxHeight;
            mIsReseting = false;
            requestLayout();
        }
    }
}
