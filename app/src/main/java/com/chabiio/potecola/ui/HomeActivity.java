package com.chabiio.potecola.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.behavior.RLSheetBehavior;
import com.chabiio.potecola.ui.behavior.TopSheetBehavior;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        final TopSheetBehavior behavior = TopSheetBehavior.from(findViewById(R.id.sheet));
//        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (behavior.getState() == TopSheetBehavior.STATE_HIDDEN)
//                    behavior.setState(TopSheetBehavior.STATE_COLLAPSED);
//            }
//        });

        final RLSheetBehavior behavior = RLSheetBehavior.from(findViewById(R.id.sheet));
        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == RLSheetBehavior.STATE_HIDDEN)
                    behavior.setState(RLSheetBehavior.STATE_COLLAPSED);
            }
        });
    }
}
