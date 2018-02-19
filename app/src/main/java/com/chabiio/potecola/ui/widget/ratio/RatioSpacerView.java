package com.chabiio.potecola.ui.widget.ratio;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.chabiio.potecola.R;

/**
 * Created by lionm on 2/18/2018.
 */

public class RatioSpacerView extends View {

    private RatioViewSizeMeasurer measurer;

    public RatioSpacerView(Context context) {
        this(context, null);
        init(RatioViewSizeMeasurer.RATIO_NATURAL, RatioViewSizeMeasurer.RATIO_NATURAL);
    }

    public RatioSpacerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public RatioSpacerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ratio_Params);
        int widthRatio = a.getInteger(R.styleable.Ratio_Params_ratio_widthRatio, RatioViewSizeMeasurer.RATIO_NATURAL);
        int heightRatio = a.getInteger(R.styleable.Ratio_Params_ratio_heightRatio, RatioViewSizeMeasurer.RATIO_NATURAL);
        a.recycle();
        init(widthRatio, heightRatio);
    }

    private void init(int widthRatio, int heightRatio) {
        measurer = new RatioViewSizeMeasurer();
        measurer.setRatio(widthRatio, heightRatio);
        setWillNotDraw(true);
        setVisibility(INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measurer.measue(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(measurer.getDeterminedWidthMeasureSpec(), measurer.getDeterminedHeightMeasureSpec());
    }
}
