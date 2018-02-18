package com.chabiio.potecola.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lionm on 2/18/2018.
 */

public interface ScrollInteractionListener {
    void onScroll(RecyclerView scroll, int dx, int dy);
}
