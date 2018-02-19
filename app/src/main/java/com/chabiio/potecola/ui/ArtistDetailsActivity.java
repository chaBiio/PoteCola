package com.chabiio.potecola.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.adapter.AlbumAdapter;
import com.chabiio.potecola.ui.recyclerview.LinearItemOffsetDecoration;
import com.chabiio.potecola.ui.recyclerview.LinearItemSpaceDecoration;
import com.chabiio.potecola.util.Res;

public class ArtistDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);

        // fixme; fitSystemWindows="true" is not working well...
        // The bottom of the list will be drawn under NavigationBar
        // because 'android:windowTranslucentNavigation' is true.
        // But fitSystemWindows="true" is not working well, so add top
        // and bottom margins or padding to contents manually.
        Res res = new Res(this);
        final int statusBarHeight = res.getStatusBarHeight();
        final int navigationBarHeight = res.getNavigationBarHeight();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPadding(toolbar.getPaddingLeft(), toolbar.getPaddingTop() + statusBarHeight,
                toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(null);
            actionBar.setSubtitle(null);
        }

        RecyclerView grid = findViewById(R.id.grid);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) grid.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin,
                params.rightMargin, params.bottomMargin + navigationBarHeight);
        grid.setLayoutParams(params);
        grid.setLayoutManager(new LinearLayoutManager(this, GridLayoutManager.HORIZONTAL, false));
        grid.setAdapter(new AlbumAdapter(this, R.layout.item_album_horizontal));

        LinearItemOffsetDecoration offsetDecoration = new LinearItemOffsetDecoration();
        offsetDecoration.setOrientation(LinearLayoutManager.HORIZONTAL);
        offsetDecoration.leftOffset = res.getDimInPx(R.dimen.artist_details_list_left_offset);
        offsetDecoration.topOffset = res.getDimInPx(R.dimen.artist_details_list_top_offset);
        offsetDecoration.rightOffset = res.getDimInPx(R.dimen.artist_details_list_right_offset);
        offsetDecoration.bottomOffset = res.getDimInPx(R.dimen.artist_details_list_bottom_offset);
        grid.addItemDecoration(offsetDecoration);

        LinearItemSpaceDecoration spaceDecoration = new LinearItemSpaceDecoration();
        spaceDecoration.setOrientation(LinearLayoutManager.HORIZONTAL);
        spaceDecoration.setSpace(res.getDimInPx(R.dimen.artist_details_list_items_space));
        grid.addItemDecoration(spaceDecoration);
    }
}
