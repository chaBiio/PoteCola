package com.chabiio.potecola.ui;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.adapter.SongAdapter;
import com.chabiio.potecola.ui.recyclerview.LinearItemOffsetDecoration;
import com.chabiio.potecola.util.Res;

public class AlbumDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(null);
            actionBar.setSubtitle(null);
        }

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new SongAdapter());

        // fixme; fitSystemWindows="true" is not working well...
        // The bottom of the list will be drawn under NavigationBar
        // because 'android:windowTranslucentNavigation' is true.
        // But fitSystemWindows="true" is not working well, so add top
        // and bottom margins to contents manually.
        Res res = new Res(this);
        final int statusBarHeight = res.getStatusBarHeight();
        final int navigationBarHeight = res.getNavigationBarHeight();

        LinearItemOffsetDecoration offsetDecoration = new LinearItemOffsetDecoration();
        offsetDecoration.bottomOffset = res.getDimInPx(R.dimen.album_details_list_bottom_offset)
                + navigationBarHeight + statusBarHeight;
        list.addItemDecoration(offsetDecoration);

        View header = findViewById(R.id.header);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) header.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + statusBarHeight,
                params.rightMargin, params.bottomMargin);
        header.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("mylog", "onBackPressed");
    }
}
