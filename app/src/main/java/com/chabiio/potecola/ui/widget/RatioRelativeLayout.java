package com.chabiio.potecola.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.chabiio.potecola.R;

/**
 * Created by lionm on 2/18/2018.
 */

public class RatioRelativeLayout extends RelativeLayout {

    public static final int RATIO_NATURAL = -1;

    private int widthRatio = RATIO_NATURAL;
    private int heightRatio = RATIO_NATURAL;

    public RatioRelativeLayout(Context context) {
        this(context, null);
    }

    public RatioRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public RatioRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioRelativeLayout);
        widthRatio = a.getInteger(R.styleable.RatioRelativeLayout_layout_widthRatio, RATIO_NATURAL);
        heightRatio = a.getInteger(R.styleable.RatioRelativeLayout_layout_heightRatio, RATIO_NATURAL);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = wSize;
        int newHeight = hSize;

        if (wMode == MeasureSpec.EXACTLY && hMode == MeasureSpec.EXACTLY ||
                (wMode == MeasureSpec.UNSPECIFIED && hMode == MeasureSpec.UNSPECIFIED) ||
                (widthRatio < 1 || heightRatio < 1)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;

        } else if (wMode == MeasureSpec.AT_MOST && hMode == MeasureSpec.AT_MOST) {
            if (wSize < hSize && widthRatio > heightRatio) {
                newHeight = (int) (hSize * (float) heightRatio / widthRatio);
            } else if (wSize > hSize && widthRatio < heightRatio) {
                newWidth = (int) (wSize * (float) widthRatio / heightRatio);
            } else if (widthRatio < heightRatio) {
                newHeight = (int) Math.min(hSize, wSize * (float) heightRatio / widthRatio);
            } else {
                newWidth = (int) Math.min(wSize, hSize * (float) widthRatio / heightRatio);
            }

        } else if (wMode == MeasureSpec.EXACTLY) {
            newHeight = (int) (hMode == MeasureSpec.AT_MOST
                    ? Math.min(hSize, wSize * (float) heightRatio / widthRatio)
                    : wSize * (float) heightRatio / widthRatio);

        } else if (wMode == MeasureSpec.AT_MOST) {
            if (hMode == MeasureSpec.EXACTLY) {
                newWidth = (int) Math.min(wSize, hSize * (float) widthRatio / heightRatio);
            } else {
                newHeight = (int) (wSize * (float) heightRatio / widthRatio);
            }

        } else { // wMode == MeasureSpec.UNSPECIFIED
            newWidth = (int) (hSize * (float) widthRatio / heightRatio);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
    }
}
