package com.nd.dwebview.wrapper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.nd.util.DisplayUtil;


/**
 * @author Android
 * @date 2017/8/25
 */

public class WebHorizontalProgressBar extends View {

    /**
     * 停在等待的百分比节点
     */
    public static final float STATE_WAITING_PERCENT = 0.8f;
    public static final float DEFAULT_HEIGHT = 3;


    private Paint mPaint;
    private float mCurrentStopX;
    private ValueAnimator mValueAnimator;
    private ValueAnimator mStopValueAnimator;
    private int mColor = Color.parseColor("#2765C3");
    private OnProgressStopFinishedListener mOnProgressStopFinishedListener;

    public WebHorizontalProgressBar(Context context) {
        super(context);
    }

    public WebHorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebHorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
//        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getHeight());
    }


    public void setColor(int color) {
        mColor = color;
    }

    public void setOnProgressStopFinishedListener(OnProgressStopFinishedListener onProgressStopFinishedListener) {
        mOnProgressStopFinishedListener = onProgressStopFinishedListener;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultHeight = DisplayUtil.dp2px(getContext(), DEFAULT_HEIGHT);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, defaultHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (mValueAnimator == null) {
//            start();
//        } else
        canvas.drawLine(0, getHeight() / 2, mCurrentStopX, getHeight() / 2, mPaint);
    }

    public void start() {
        setVisibility(VISIBLE);
        setAlpha(1f);
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mCurrentStopX = 0.0f;
        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mCurrentStopX = value * getWidth() * STATE_WAITING_PERCENT;
                invalidate();
            }
        });
        mValueAnimator.setDuration(1000);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.start();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }, 5000);
    }

    public void stop() {
        if (getVisibility() == GONE || mValueAnimator == null || mStopValueAnimator != null) {
            return;
        }
        mValueAnimator.cancel();
        //计算剩余像素长度
        final float remainWidth = getWidth() - mCurrentStopX;
        mStopValueAnimator = ValueAnimator.ofFloat(0, 1);
        mStopValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mCurrentStopX = (getWidth() - remainWidth) + value * remainWidth;
                setAlpha(1 - (value * 10 / 15));
                invalidate();
            }
        });
        mStopValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                mStopValueAnimator = null;
                if (mOnProgressStopFinishedListener != null) {
                    mOnProgressStopFinishedListener.onProgressStopFinished();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mStopValueAnimator.setDuration(800);
        mStopValueAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mStopValueAnimator.start();
    }

    public interface OnProgressStopFinishedListener {
        /**
         * 当进度结束
         */
        void onProgressStopFinished();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getHandler().removeCallbacksAndMessages(null);
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        if (mStopValueAnimator != null) {
            mStopValueAnimator.cancel();
        }
    }
}
