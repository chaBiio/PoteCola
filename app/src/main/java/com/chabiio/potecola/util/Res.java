package com.chabiio.potecola.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DimenRes;
import android.support.annotation.Px;

/**
 * Created by lionm on 2/19/2018.
 */

public class Res {

    private final Context context;

    public Res(Context context) {
        this.context = context;
    }

    @Px
    public int getDimInPx(@DimenRes int id) {
        return context.getResources().getDimensionPixelSize(id);
    }

    @Px
    public int getStatusBarHeight() {
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getDimInPx(resourceId) : 0;
    }

    @Px
    public int getActionBarHeight() {
        int actionBarHeight;
        final TypedArray styledAttributes = context.getTheme()
                .obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }

    @Px
    public int getNavigationBarHeight() {
        int resourceId = context.getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? getDimInPx(resourceId) : 0;
    }
}
