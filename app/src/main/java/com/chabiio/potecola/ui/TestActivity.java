package com.chabiio.potecola.ui;

import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;

import com.chabiio.potecola.R;
import com.chabiio.potecola.util.Res;

public class TestActivity extends AppCompatActivity {

    private final View.OnTouchListener touchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        initView();
    }

    private void initView() {
        // fixme; fitSystemWindows="true" is not working well...
        // fitSystemWindows="true" is not working well, so add top
        // and bottom margins or padding to contents manually.
        final Res res = new Res(this);
        final View controlPanel = findViewById(R.id.control_panel);
        controlPanel.setPadding(
                controlPanel.getPaddingLeft(), controlPanel.getPaddingTop(), controlPanel.getPaddingRight(),
                controlPanel.getPaddingBottom() + res.getNavigationBarHeight());

        // Make the SeekBar full width.
        final SeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setPadding(0, seekBar.getPaddingTop(), 0, seekBar.getPaddingBottom());

        // Adjust view positions
        final View menuPanel = findViewById(R.id.menu_panel);
        final BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(menuPanel);
        final View root = findViewById(R.id.cl_root);
        ViewTreeObserver observer = root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int controlPanelHeight = controlPanel.getHeight();

                // Adjust seekBar's vertical position.
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) seekBar.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                        controlPanelHeight - seekBar.getHeight() / 2);
                seekBar.setLayoutParams(params);

                menuPanel.setPadding(menuPanel.getPaddingLeft(), menuPanel.getPaddingTop(), menuPanel.getPaddingRight(),
                        menuPanel.getPaddingBottom() + controlPanelHeight);
                menuPanel.invalidate();

                // Adjust BottomSheet's peek height.
                sheetBehavior.setPeekHeight(controlPanelHeight + res.getDimInPx(R.dimen.unit_music_player_song_info_panel_height));
//                sheetBehavior.setPeekHeight(root.getHeight() - root.getWidth());
            }
        });

//        ViewCompat.setElevation(controlPanel, res.getDimInPx(R.dimen.music_player_control_panel_elevation));
//        ViewCompat.setElevation(seekBar, res.getDimInPx(R.dimen.music_player_control_panel_elevation));
//        ViewCompat.setElevation(menuPanel, res.getDimInPx(R.dimen.music_player_menu_panel_elevation));

        // Avoid unwanted scrolling of the bottom sheet
        controlPanel.setOnTouchListener(touchEater);
    }
}
