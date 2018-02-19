package com.chabiio.potecola.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.adapter.AlbumAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment {

    private ScrollInteractionListener scrollInteractionListener;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RecyclerView grid = (RecyclerView) inflater.inflate(R.layout.grid, container, false);

        grid.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false));
        grid.setAdapter(new AlbumAdapter(getActivity(), R.layout.item_album_vertical));
        grid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (scrollInteractionListener != null) {
                    scrollInteractionListener.onScroll(recyclerView, dx, dy);
                }
            }
        });

        return grid;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            scrollInteractionListener = (ScrollInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implements "
                    + ScrollInteractionListener.class.getSimpleName() + " interface");
        }
    }
}
