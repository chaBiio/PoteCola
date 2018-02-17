package com.chabiio.potecola.ui;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.widget.TopSheetBehavior;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final TopSheetBehavior behavior = TopSheetBehavior.from(findViewById(R.id.sheet));
        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == TopSheetBehavior.STATE_HIDDEN)
                    behavior.setState(TopSheetBehavior.STATE_COLLAPSED);
            }
        });
    }
}
