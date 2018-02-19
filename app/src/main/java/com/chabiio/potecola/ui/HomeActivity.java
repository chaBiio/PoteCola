package com.chabiio.potecola.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.behavior.AbsSheetBehavior;
import com.chabiio.potecola.ui.behavior.RLSheetBehavior;
import com.chabiio.potecola.ui.behavior.TopSheetBehavior;
import com.chabiio.potecola.util.Res;

public class HomeActivity extends AppCompatActivity implements ScrollInteractionListener {

    private View scrim;
    private TopSheetBehavior topSheetBehavior;
    private RLSheetBehavior sideSheetBehavior;
    private ScrollInteractionListener scrollInteractionListener;

    private final AbsSheetBehavior.SheetCallback sideSheetCallback = new AbsSheetBehavior.SheetCallback() {
        @Override
        public void onStateChanged(@NonNull View sheet, int newState) {}

        @Override
        public void onSlide(@NonNull View sheet, float slideOffset) {
            if (slideOffset == 0.f && scrim.isShown()) {
                scrim.setVisibility(View.GONE);
            } else if (0 < slideOffset) {
                scrim.setAlpha(slideOffset);
                if (!scrim.isShown()) {
                    scrim.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();

//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, new AlbumsFragment()).commit();
    }

    private void initView() {
        ViewGroup fragmentContainer = findViewById(R.id.fragment_container);
        View contentsContainer = findViewById(R.id.contents_container);
        View topSheet = findViewById(R.id.top_sheet);
        View sideSheet = findViewById(R.id.side_sheet);
        scrim = findViewById(R.id.scrim);

        topSheetBehavior = TopSheetBehavior.from(topSheet);
        sideSheetBehavior = RLSheetBehavior.from(sideSheet);
        sideSheetBehavior.setSheetCallback(sideSheetCallback);

        // peek height of the top sheet
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final int peekHeight = (int) (displaySize.x * 9.0 / 16.0);
        topSheetBehavior.setPeekHeight(peekHeight);

        // fixme; fitSystemWindows="true" is not working well...
        // fitSystemWindows="true" is not working well, so add top
        // and bottom margins or padding to contents manually.

        Res res = new Res(this);
        final int statusBarHeight = res.getStatusBarHeight();
        final int navigationBarHeight = res.getNavigationBarHeight();

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) topSheet.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + statusBarHeight,
                params.rightMargin, params.bottomMargin);
        topSheet.setLayoutParams(params);

        params = (CoordinatorLayout.LayoutParams) sideSheet.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + statusBarHeight,
                params.rightMargin, params.bottomMargin);
        sideSheet.setLayoutParams(params);

        // Add elevation to the top & side sheet
        ViewCompat.setElevation(topSheet, getResources().getDimensionPixelSize(R.dimen.home_top_sheet_elevation));
        ViewCompat.setElevation(sideSheet, getResources().getDimensionPixelSize(R.dimen.home_side_sheet_elevation));
    }

    /**
     * region; ScrollInteractionListener interface implementation
     */

    @Override
    public void onScroll(RecyclerView scroll, int dx, int dy) {
//        if (dy > 20) {
//            topSheetBehavior.setState(AbsSheetBehavior.STATE_HIDDEN);
//            sideSheetBehavior.setState(AbsSheetBehavior.STATE_HIDDEN);
//        } else if (dy < -20) {
//            topSheetBehavior.setState(AbsSheetBehavior.STATE_COLLAPSED);
//            sideSheetBehavior.setState(AbsSheetBehavior.STATE_COLLAPSED);
//        }
    }
}
