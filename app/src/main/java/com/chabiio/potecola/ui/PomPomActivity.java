package com.chabiio.potecola.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.chabiio.potecola.R;
import com.chabiio.potecola.ui.widget.MeltingView;

public class PomPomActivity extends AppCompatActivity {

    MeltingView meltingView;
    boolean filled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pom_pom);
        meltingView = findViewById(R.id.melting);
        findViewById(R.id.anim_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meltingView.startMeltingAnimation(new AccelerateInterpolator(), filled);
                filled = !filled;
            }
        });
    }
}
