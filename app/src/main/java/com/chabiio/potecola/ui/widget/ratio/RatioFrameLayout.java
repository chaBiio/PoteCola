package com.chabiio.potecola.ui.widget.ratio;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chabiio.potecola.R;

/**
 * Created by lionm on 2/19/2018.
 */

public class RatioFrameLayout extends FrameLayout {


    private RatioViewSizeMeasurer measurer;

    public RatioFrameLayout(Context context) {
        this(context, null);
        init(RatioViewSizeMeasurer.RATIO_NATURAL, RatioViewSizeMeasurer.RATIO_NATURAL);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(int widthRatio, int heightRatio) {
        measurer = new RatioViewSizeMeasurer();
        measurer.setRatio(widthRatio, heightRatio);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ratio_Params);
        int widthRatio = a.getInteger(R.styleable.Ratio_Params_ratio_widthRatio, RatioViewSizeMeasurer.RATIO_NATURAL);
        int heightRatio = a.getInteger(R.styleable.Ratio_Params_ratio_heightRatio, RatioViewSizeMeasurer.RATIO_NATURAL);
        a.recycle();
        init(widthRatio, heightRatio);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measurer.measue(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(measurer.getDeterminedWidthMeasureSpec(), measurer.getDeterminedHeightMeasureSpec());
    }

}
