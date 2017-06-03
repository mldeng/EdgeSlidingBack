package com.geejoe.edgeslidingback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by JoeLee on 2017/6/3 0003 09:00.
 */

public class EdgeSlidingBackLayout extends FrameLayout {

    private EdgeSlidingBackActivity mActivity;
    private EdgeSlidingBackFragment mFragment;

    private Scroller mScroller;

    private int mScreenWidth;
    //可滑动返回的操作区域宽度
    private int mTouchAreaWidth;
    //有效滑动距离
    private int mTouchSlope;

    //解决滑动冲突
    private int mLastX;
    private int mLastY;
    private int mActionDownX;

    private Drawable mShadow;
    private int mShadowWidth;

    public EdgeSlidingBackLayout(Context context) {
        this(context, null, 0);
    }

    public EdgeSlidingBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EdgeSlidingBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        //屏幕宽度的十二分之一作为可滑动返回的操作区域宽度
        mTouchAreaWidth = mScreenWidth / 12;
        mTouchSlope = ViewConfiguration.get(context).getScaledTouchSlop();
        mShadow = context.getResources().getDrawable(R.drawable.left_shadow);
        mShadowWidth = (int)context.getResources().getDisplayMetrics().density*16;
    }

    public void bindActivity(EdgeSlidingBackActivity activity) {
        this.mActivity = activity;
        //将EdgeSlidingBackLayout作为最外层布局
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View rootView = decorView.getChildAt(0);
        decorView.removeView(rootView);
        addView(rootView);
        decorView.addView(this);
//        this.setBackgroundColor(Color.TRANSPARENT);
//        decorView.setBackgroundColor(Color.TRANSPARENT);
    }

    public void bindFrgment(EdgeSlidingBackFragment fragment) {
        this.mFragment = fragment;
        // TODO
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownX = (int) ev.getX();
                mLastX = x;
                mLastY = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (mActionDownX < mTouchAreaWidth && deltaX > mTouchSlope
                        && Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (mActionDownX < mTouchAreaWidth
                        && Math.abs(deltaX) > Math.abs(deltaY)) {
                    int moveX = -deltaX;
                    if (-getScrollX() < 0) {
                        scrollTo(0, 0);
                    } else {
                        scrollBy(moveX, 0);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (-getScrollX() < mScreenWidth / 3) {
                    scrollResume();
                } else {
                    scrollRightOut();
                }
                break;
        }
        return true;
    }

    private void scrollResume() {
        int startX = getScrollX();
        int distance = -getScrollX();
        mScroller.startScroll(startX, 0, distance, 0, 300);
        invalidate();
    }

    private void scrollRightOut() {
        int startX = getScrollX();
        int distance = -mScreenWidth - startX;

        mScroller.startScroll(startX, 0, distance, 0, 300);
        invalidate();
        mActivity.finish();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShadow(canvas);
    }

    private void drawShadow(Canvas canvas) {
        mShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mShadow.draw(canvas);
        canvas.restore();
    }
}
