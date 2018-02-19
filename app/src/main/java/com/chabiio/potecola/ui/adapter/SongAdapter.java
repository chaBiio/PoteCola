package com.chabiio.potecola.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chabiio.potecola.R;

/**
 * Created by lionm on 2/19/2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private String[] DATA = new String[] {
      "CROSS ROAD",
            "PADDLE",
            "World's end",
            "Center Of Universe",
            "Over",
            "Not Found",
            "Reflection",
            "I can make it",
            "It's a wonderful world",
            "overtrue",
            "Progress",
            "I want you back",
            "Another Story",
            "Bird cage",
            "Dive",
            "Prism",
            "Starting Over"
    };

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_with_index, parent, false));
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        holder.songIndex.setText(String.valueOf(position + 1));
        holder.songTitle.setText(DATA[position]);
        holder.songSubTitle.setText("6:42");
    }

    @Override
    public int getItemCount() {
        return DATA.length;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        final TextView songTitle;
        final TextView songSubTitle;
        final TextView songIndex;

        public SongViewHolder(View view) {
            super(view);
            songTitle = view.findViewById(R.id.title);
            songSubTitle = view.findViewById(R.id.sub_title);
            songIndex = view.findViewById(R.id.index);
        }
    }
}
