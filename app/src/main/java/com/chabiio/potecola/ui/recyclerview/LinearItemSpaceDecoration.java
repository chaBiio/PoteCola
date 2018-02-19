package com.chabiio.potecola.ui.recyclerview;

import android.graphics.Rect;
import android.support.annotation.Px;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lionm on 2/19/2018.
 */

public class LinearItemSpaceDecoration extends RecyclerView.ItemDecoration {

    @Px private int spaceDiv2;
    private int orientation;

    public LinearItemSpaceDecoration() {
        this(0, LinearLayoutManager.VERTICAL);
    }

    public LinearItemSpaceDecoration(@Px int space, int orientation) {
        this.spaceDiv2 = space / 2;
        this.orientation = orientation;
    }

    public void setSpace(@Px int space) {
        spaceDiv2 = space / 2;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final int position = parent.getChildLayoutPosition(view);
        if (orientation == LinearLayoutManager.VERTICAL) {
            if (position != 0) {
                outRect.top = spaceDiv2;
            }
            if (position != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = spaceDiv2;
            }

        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            if (position != 0) {
                outRect.left = spaceDiv2;
            }
            if (position != parent.getAdapter().getItemCount() - 1) {
                outRect.right = spaceDiv2;
            }
        }
    }
}
