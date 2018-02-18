package com.chabiio.potecola.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chabiio.potecola.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by lionm on 2/18/2018.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final Context context;

    public AlbumAdapter(Context context) {
        this.context = context;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artwork_regular, parent, false));
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 200;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        final ImageView artwrok;

        public AlbumViewHolder(View view) {
            super(view);
            artwrok = view.findViewById(R.id.artwork);
        }
    }
}
