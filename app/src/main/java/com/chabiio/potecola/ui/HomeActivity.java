package com.chabiio.potecola.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.behavior.RLSheetBehavior;
import com.chabiio.potecola.ui.behavior.TopSheetBehavior;

public class HomeActivity extends AppCompatActivity {

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

        TopSheetBehavior topSheetBehavior = TopSheetBehavior.from(topSheet);
        RLSheetBehavior sideSheetBehavior = RLSheetBehavior.from(sideSheet);

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
    }
}
