package com.cwc.refreshview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

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
    private Paint mLinePaint;
    private Path mPath;
    private Context mContext;
    private int mMeasureHeight;
    private int mLineLocationY;
    private int mBitmapWidth;

    private int multiple = 3;
    private Bitmap mBitmap;

    private boolean mIsReseting = false;
    private static final int INVALIDATE_CODE = 1;
    private static final int RESET_IMAGE_CODE = 2;

    private static final int STATE_BEYOND_RISE = 0;
    private static final int STATE_LESS_RISE = 1;
    private static final int STATE_LESS_DROP = 2;
    private int v1;
    private int v2;
    private int v3;

    private int mState;


    public MyRefreshView(Context context) {
        this(context, null);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mMaxHeight = (int) getResources().getDimension(R.dimen.height);
        mLocalHeight = mHeight = mMaxHeight;
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.blue));
        mPaint.setAntiAlias(true);
        mPath = new Path();
        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.blue));
        mLinePaint.setAntiAlias(true);
        mBitmapWidth = (int) getResources().getDimension(R.dimen.image_width);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        PointF start = new PointF(0, mMaxHeight);
        PointF end = new PointF(mWidth, mMaxHeight);
        PointF control = new PointF(mWidth / 2, mHeight);
        PointF highest = getBezierPointF((float) 0.5, start, end, control);
        mMeasureHeight = mLineLocationY = (int) highest.y;
        mMeasureHeight = mMeasureHeight + mBitmapWidth / 2;
        mMeasureHeight = mMeasureHeight > mMaxHeight ? mMeasureHeight : mMaxHeight;
        setMeasuredDimension(mWidth, mMeasureHeight);
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
        canvas.drawPath(mPath, mLinePaint);
        RectF rectF = new RectF((mWidth - mBitmapWidth) / 2, mLineLocationY - mBitmapWidth / 2, (mWidth +
                mBitmapWidth) / 2, mLineLocationY + mBitmapWidth / 2);
        canvas.drawBitmap(mBitmap, null, rectF, null);
    }

    /**
     * 下拉过程中根据手指下拉距离改变状态
     *
     * @param height 手指下拉距离
     */
    public void addHeight(int height){
        mHeight = mLocalHeight;
        mDragHeight = height / multiple;
        mHeight += mDragHeight;
        requestLayout();
    }

    /**
     * 手指松开之后重置
     */
    public void reset(){
        mIsReseting = true;
        resetView();
    }

    /**
     * 回弹过程
     */
    public void resetView(){
        v1 = 0;
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case INVALIDATE_CODE:
                        requestLayout();
                        invalidate();
                        break;
                    case RESET_IMAGE_CODE:
                        break;
                }
            }
        };

        mState = STATE_BEYOND_RISE;
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                switch (mState) {
                    case STATE_BEYOND_RISE:
                        //这里有问题
                        if (mHeight > mMaxHeight) {
                            v1 += 2;
                            mHeight -= v1;
                            if (mHeight < mMaxHeight) {
                                mHeight = mMaxHeight;
                            }
                            handler.sendEmptyMessage(INVALIDATE_CODE);
                        } else {
                            mHeight = mMaxHeight;
                            mLocalHeight = mMaxHeight;
                            mIsReseting = false;
                            handler.sendEmptyMessage(INVALIDATE_CODE);
                            mState = STATE_LESS_RISE;
                            mLinePaint.setColor(0xffffffff);
                        }
                        break;
                    case STATE_LESS_RISE:
                        if (v1 > 0) {
                            v1 -= 6;
                            mHeight -= v1;
                            handler.sendEmptyMessage(INVALIDATE_CODE);
                        } else {
                            mState = STATE_LESS_DROP;
                            mLinePaint.setColor(0xffffffff);
                        }
                        break;
                    case STATE_LESS_DROP:
                        if ((mHeight + v1) < mMaxHeight) {
                            v1 += 2;
                            mHeight += v1;
                            handler.sendEmptyMessage(INVALIDATE_CODE);
                        } else {
                            mHeight = mMaxHeight;
                            mLocalHeight = mMaxHeight;
                            handler.sendEmptyMessage(INVALIDATE_CODE);
                            handler.sendEmptyMessage(RESET_IMAGE_CODE);
                            mState = STATE_BEYOND_RISE;
                            mLinePaint.setColor(getResources().getColor(R.color.blue));
                            timer.cancel();
                        }
                        break;
                    default:
                        break;
                }
            }
        }, 0, 20);
    }

}
