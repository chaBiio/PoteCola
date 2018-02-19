package com.chabiio.potecola.ui.widget.ratio;

import android.view.View;

/**
 * Created by lionm on 2/18/2018.
 */

class RatioViewSizeMeasurer {

    static final int RATIO_NATURAL = -1;
    private int widthRatio = RATIO_NATURAL;
    private int heightRatio = RATIO_NATURAL;
    private int determinedWidthMeasureSpec;
    private int determinedHeightMeasureSpec;

    void setRatio(int widthRatio, int heightRatio) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    int getDeterminedWidthMeasureSpec() {
        return determinedWidthMeasureSpec;
    }

    int getDeterminedHeightMeasureSpec() {
        return determinedHeightMeasureSpec;
    }

    void measue(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int wSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int hMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int hSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = wSize;
        int newHeight = hSize;

        if (wMode == View.MeasureSpec.EXACTLY && hMode == View.MeasureSpec.EXACTLY ||
                (wMode == View.MeasureSpec.UNSPECIFIED && hMode == View.MeasureSpec.UNSPECIFIED) ||
                (widthRatio < 1 || heightRatio < 1)) {
            determinedWidthMeasureSpec = widthMeasureSpec;
            determinedHeightMeasureSpec = heightMeasureSpec;
            return;

        } else if (wMode == View.MeasureSpec.AT_MOST && hMode == View.MeasureSpec.AT_MOST) {
            if (wSize < hSize && widthRatio > heightRatio) {
                newHeight = (int) (hSize * (float) heightRatio / widthRatio);
            } else if (wSize > hSize && widthRatio < heightRatio) {
                newWidth = (int) (wSize * (float) widthRatio / heightRatio);
            } else if (widthRatio < heightRatio) {
                newHeight = (int) Math.min(hSize, wSize * (float) heightRatio / widthRatio);
            } else {
                newWidth = (int) Math.min(wSize, hSize * (float) widthRatio / heightRatio);
            }

        } else if (wMode == View.MeasureSpec.EXACTLY) {
            newHeight = (int) (hMode == View.MeasureSpec.AT_MOST
                    ? Math.min(hSize, wSize * (float) heightRatio / widthRatio)
                    : wSize * (float) heightRatio / widthRatio);

        } else if (wMode == View.MeasureSpec.AT_MOST) {
            if (hMode == View.MeasureSpec.EXACTLY) {
                newWidth = (int) Math.min(wSize, hSize * (float) widthRatio / heightRatio);
            } else {
                newHeight = (int) (wSize * (float) heightRatio / widthRatio);
            }

        } else { // wMode == MeasureSpec.UNSPECIFIED
            newWidth = (int) (hSize * (float) widthRatio / heightRatio);
        }

        determinedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
        determinedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(newHeight, View.MeasureSpec.EXACTLY);
    }
}
