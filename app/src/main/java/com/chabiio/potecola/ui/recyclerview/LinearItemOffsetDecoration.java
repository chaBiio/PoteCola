package com.chabiio.potecola.ui.recyclerview;

import android.graphics.Rect;
import android.support.annotation.Px;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lionm on 2/19/2018.
 */

public class LinearItemOffsetDecoration extends RecyclerView.ItemDecoration {

    @Px public int topOffset;
    @Px public int bottomOffset;
    @Px public int leftOffset;
    @Px public int rightOffset;
    private int orientation = LinearLayoutManager.VERTICAL;

    public LinearItemOffsetDecoration() {
        this(0, 0, 0, 0);
    }

    public LinearItemOffsetDecoration(@Px int leftOffset, @Px int topOffset, @Px int rightOffset, @Px int bottomOffset) {
        this.leftOffset = leftOffset;
        this.topOffset = topOffset;
        this.rightOffset = rightOffset;
        this.bottomOffset = bottomOffset;
    }

    public void setOffsets(int left, int top, int right, int bottom) {
        leftOffset = left;
        topOffset = top;
        rightOffset = right;
        bottomOffset = bottom;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.left = leftOffset;
            outRect.right = rightOffset;
            int position = parent.getChildLayoutPosition(view);
            if (position == 0) {
                outRect.top = topOffset;
            } else if (position == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = bottomOffset;
            }

        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            outRect.top = topOffset;
            outRect.bottom = bottomOffset;
            int position = parent.getChildLayoutPosition(view);
            if (position == 0) {
                outRect.left = leftOffset;
            } else if (position == parent.getAdapter().getItemCount() - 1) {
                outRect.right = rightOffset;
            }
        }
    }
}
