package com.chabiio.potecola.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.behavior.AbsSheetBehavior;
import com.chabiio.potecola.ui.behavior.RLSheetBehavior;
import com.chabiio.potecola.ui.behavior.TopSheetBehavior;

public class HomeActivity extends AppCompatActivity {

    private View scrim;

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
    }

    private void initView() {
        ViewGroup fragmentContainer = findViewById(R.id.fragment_container);
        View contentsContainer = findViewById(R.id.contents_container);
        View topSheet = findViewById(R.id.top_sheet);
        View sideSheet = findViewById(R.id.side_sheet);
        scrim = findViewById(R.id.scrim);

        TopSheetBehavior topSheetBehavior = TopSheetBehavior.from(topSheet);
        RLSheetBehavior sideSheetBehavior = RLSheetBehavior.from(sideSheet);
        sideSheetBehavior.setSheetCallback(sideSheetCallback);

        // peek height of the top sheet
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final int peekHeight = (int) (displaySize.x * 9.0 / 16.0);
        topSheetBehavior.setPeekHeight(peekHeight);

        // Add top margins to the bounds of main contents and the side sheet.
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) contentsContainer.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + peekHeight,
                params.rightMargin, params.bottomMargin);
        contentsContainer.setLayoutParams(params);
        params = (CoordinatorLayout.LayoutParams) sideSheet.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + peekHeight,
                params.rightMargin, params.bottomMargin);
        sideSheet.setLayoutParams(params);

        // Add elevation to the top & side sheet
        ViewCompat.setElevation(topSheet, getResources().getDimensionPixelSize(R.dimen.home_top_sheet_elevation));
        ViewCompat.setElevation(sideSheet, getResources().getDimensionPixelSize(R.dimen.home_side_sheet_elevation));
    }
}
