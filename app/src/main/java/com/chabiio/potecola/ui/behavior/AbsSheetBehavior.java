package com.chabiio.potecola.ui.behavior;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lionm on 2/17/2018.
 */

abstract public class AbsSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    abstract public static class SheetCallback {
        /**
         * Called when the bottom sheet changes its state.
         *
         * @param sheet The bottom sheet view.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        public abstract void onStateChanged(@NonNull View sheet, @State int newState);
        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param sheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
         *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         *                    between hidden and collapsed states.
         */
        public abstract void onSlide(@NonNull View sheet, float slideOffset);
    }

    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;
    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = 2;
    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = 3;
    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 4;
    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = 5;

    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private SheetCallback mCallback;

    public AbsSheetBehavior() {}

    public AbsSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSheetCallback(SheetCallback callback) {
        mCallback = callback;
    }

    SheetCallback getCallback() {
        return mCallback;
    }
}
