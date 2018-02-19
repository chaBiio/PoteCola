package com.chabiio.potecola.ui.recyclerview;

import android.graphics.Rect;
import android.support.annotation.Px;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lionm on 2/19/2018.
 */

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    public interface Callback {
        boolean isLastItem(int position);
    }

    @Px public int topOffset;
    @Px public int bottomOffset;
    @Px public int leftOffset;
    @Px public int rightOffset;

    public ItemOffsetDecoration() {
        this(0, 0, 0, 0);
    }

    public ItemOffsetDecoration(@Px int leftOffset, @Px int topOffset, @Px int rightOffset, @Px int bottomOffset) {
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

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = leftOffset;
        outRect.right = rightOffset;
        int position = parent.getChildLayoutPosition(view);
        if (position == 0) {
            outRect.top = topOffset;
        } else if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = bottomOffset;
        }
    }
}
