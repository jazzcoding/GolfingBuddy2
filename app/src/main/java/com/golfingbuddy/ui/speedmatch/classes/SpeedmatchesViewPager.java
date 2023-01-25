package com.golfingbuddy.ui.speedmatch.classes;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by kairat on 1/19/15.
 */
public class SpeedmatchesViewPager extends ViewPager {
    protected Boolean mTouchEnable = true;

    public SpeedmatchesViewPager(Context context) {
        super(context);
    }

    public SpeedmatchesViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (mTouchEnable) {
            return super.onInterceptTouchEvent(arg0);
        }

        return mTouchEnable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchEnable) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    public Boolean getTouchEnable() {
        return mTouchEnable;
    }

    public void setTouchEnable(Boolean touchEnable) {
        this.mTouchEnable = touchEnable;
    }
}
