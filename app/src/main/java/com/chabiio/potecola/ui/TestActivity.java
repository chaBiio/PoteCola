package com.chabiio.potecola.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.chabiio.potecola.R;
import com.chabiio.potecola.util.Res;

import java.lang.reflect.ParameterizedType;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_music_control_panel);

        Res res = new Res(this);
        final View root = findViewById(R.id.rl_root);
        root.setPadding(root.getPaddingLeft(), root.getPaddingTop(), root.getPaddingRight(),
                root.getPaddingBottom() + res.getNavigationBarHeight());

        // Make the SeekBar full width
        final SeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setPadding(0, seekBar.getPaddingTop(), 0, seekBar.getPaddingBottom());

        // Adjust seekBar's vertical position
        final View controlPanel = findViewById(R.id.control_panel);
        ViewTreeObserver observer = root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                        controlPanel.getHeight() - seekBar.getHeight() / 2);
                seekBar.setLayoutParams(params);
            }
        });
    }
}
