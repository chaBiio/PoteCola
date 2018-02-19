package com.chabiio.potecola.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chabiio.potecola.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by lionm on 2/18/2018.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final Context context;
    @LayoutRes private final int itemLayoutId;

    public AlbumAdapter(Context context, @LayoutRes int itemLayoutId) {
        this.context = context;
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 50;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        final ImageView artwrok;
        final TextView title;
        final TextView subTitle;

        public AlbumViewHolder(View view) {
            super(view);
            artwrok = view.findViewById(R.id.artwork);
            title = view.findViewById(R.id.title);
            subTitle = view.findViewById(R.id.sub_title);
        }
    }
}
