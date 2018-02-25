package com.chabiio.potecola.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.chabiio.potecola.R;

/**
 * Created by lionm on 2/24/2018.
 */

public class MeltingView extends View {

    @ColorInt private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_DURATION = 500;
    private static final float DEFAULT_PROGRESS = 0.3f;

    private Paint paint = new Paint();
    private Path path = new Path();
    private int duration = DEFAULT_DURATION; // millis
    private float progress = DEFAULT_PROGRESS; // 0f ~ 1f
    private DensityDistributionProvider densityProvider = new DefaultDensityDistProvider();
    private Animation.AnimationListener animationListener;
    private boolean disablePreDrawing = false;
    @ColorInt private int secondaryColor;
    @ColorInt private int primaryColor;
    @ColorInt private int backgroundColor;

    public MeltingView(Context context) {
        this(context, null);
    }

    public MeltingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeltingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) readAttributes(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Drawable bg = getBackground();
        if (bg instanceof ColorDrawable) {
            backgroundColor = ((ColorDrawable) bg).getColor();
        } else {
            backgroundColor = Color.TRANSPARENT;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MeltingView, 0, 0);
        try {
            primaryColor = ta.getColor(R.styleable.MeltingView_melting_primary_color, fetchAccentColor(context));
            secondaryColor = ta.getColor(R.styleable.MeltingView_melting_secondary_color, DEFAULT_PRIMARY_COLOR);
            duration = ta.getInteger(R.styleable.MeltingView_melting_animation_duration, DEFAULT_DURATION);
            disablePreDrawing = ta.getBoolean(R.styleable.MeltingView_melting_disable_pre_drawing, false);
            setProgress(ta.getFloat(R.styleable.MeltingView_melting_progress, DEFAULT_PROGRESS));
        } finally {
            ta.recycle();
        }
    }

    public void setDuration(int duration) {
        this.duration = (duration < 0) ? 0 : duration;
        wasDurationChanged = true;
    }

    public int getDuration() {
        return duration;
    }

    public void setProgress(float progress) {
        this.progress = MathUtils.clamp(progress, 0f, 1f);
        this.invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void addProgress(float d) {
        this.progress = MathUtils.clamp(this.progress + d, 0f, 1f);
    }

    public void startMeltingAnimation(boolean reverse) {
        this.startAnimation(new MeltingAnimation(this, reverse));
    }

    public void startMeltingAnimation(Interpolator interpolator, boolean reverse) {
        this.startAnimation(new MeltingAnimation(this, interpolator, reverse));
    }

    public void setDensityDistributionProvider(DensityDistributionProvider provider) {
        if (provider != null) {
            densityProvider = provider;
            wasDensityDistProviderChanged = true;
        }
    }

    public void setPrimaryColor(@ColorInt int color) {
        primaryColor = color;
        invalidate();
    }

    public void setPrimaryColor(@ColorRes int id, Context context) {
        setPrimaryColor(ContextCompat.getColor(context, id));
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setSecondaryColor(@ColorInt int color) {
        secondaryColor = color;
        invalidate();
    }

    public void setSecondaryColor(@ColorRes int id, Context context) {
        setSecondaryColor(ContextCompat.getColor(context, id));
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor = color;
        super.setBackgroundColor(color);
    }

    public void setBackgroundColor(@ColorRes int id, Context context) {
        setBackgroundColor(ContextCompat.getColor(context, id));
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setColors(@ColorInt int primary, @ColorInt int secondary, @ColorInt int background) {
        primaryColor = primary;
        secondaryColor = secondary;
        setBackgroundColor(background);
    }

    public void setColors(@ColorRes int primary, @ColorRes int secondary, @ColorRes int background, Context context) {
        setColors(ContextCompat.getColor(context, primary),
                ContextCompat.getColor(context, secondary),
                ContextCompat.getColor(context, background));
    }

    public void disablePreDrawing(boolean disable) {
        boolean forceUpdate = disable != disablePreDrawing;
        disablePreDrawing = disable;
        if (forceUpdate) invalidate();
    }

    public boolean isPreDrawingDisabled() {
        return disablePreDrawing;
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        animationListener = listener;
    }

    public Animation.AnimationListener getAnimationListener() {
        return animationListener;
    }

    private int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private static final float MIN_DURATION_RATE_PRIMARY = 0.4f;
    private static final float MIN_DURATION_RATE_SECONDARY = 0.3f;
    private static final float DURATION_RATE_SECONDARY = 0.65f;
    @Px private static final int BASE_LINE_OFFSET = -20;
    @Px private static final int SEGMENT_WIDTH = 140;
    @Px private static final int SEGMENT_WIDTH_HALF = SEGMENT_WIDTH / 2;

    private float[] segmentYPositions;
    private float[] secondarySegmentYPositions;
    private float[] cachedDensities;
    // Cached coefficients used at calculating the y-coordinates.
    private float p1; // [maxY]/[duration]
    private float p2; // ([maxY]/[min-duration])-p1
    private float s1; // [maxY]/[pre-duration]
    private float s2; // ([maxY]/[min-pre-duration])-s1

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        prepareForDrawing(canvas);
        if (cachedDensities.length == 0) return;
        float canvasW = canvas.getWidth();
        float time = duration * progress;
        float baselineY = p1 * time + BASE_LINE_OFFSET;
        float preBaselineY = s1 * time + BASE_LINE_OFFSET;

        for (int i = 0; i < segmentYPositions.length; ++i) {
            float density = cachedDensities[i];
            segmentYPositions[i] = (p1 + p2 * density) * time - baselineY;
            if (!disablePreDrawing) {
                secondarySegmentYPositions[i] = (s1 + s2 * density) * time - preBaselineY;
            }
        }

        // Draw the background.
        canvas.drawColor(backgroundColor);

        // Draw the pre-melting.
        if (!disablePreDrawing) {
            paint.setColor(secondaryColor);
            // fixme: Add +10px to the rect to hide unnecessary border line which appears sometimes.
            canvas.drawRect(0, 0, canvasW, preBaselineY + 10, paint);
            path.reset();
            pathJoinSegmentsSmoothly(path, secondarySegmentYPositions, 0, preBaselineY);
            canvas.drawPath(path, paint);
        }

        // Draw the melting.
        paint.setColor(primaryColor);
        // fixme: Add +10px to the rect to hide unnecessary border line which appears sometimes.
        canvas.drawRect(0, 0, canvasW, baselineY + 10, paint);path.reset();
        pathJoinSegmentsSmoothly(path, segmentYPositions, 0, baselineY);
        canvas.drawPath(path, paint);
    }

    private boolean wasDensityDistProviderChanged = false;
    private boolean wasDurationChanged = false;
    private int cachedCanvasWidth = -1;
    private int cachedCanvasHeight = -1;

    private void prepareForDrawing(Canvas canvas) {
        if (cachedCanvasWidth != canvas.getWidth() || wasDensityDistProviderChanged) {
            wasDensityDistProviderChanged = false;
            cachedCanvasWidth = canvas.getWidth();
            int segmentCount = (int) Math.ceil(cachedCanvasWidth / SEGMENT_WIDTH) + 1;
            segmentYPositions = new float[segmentCount];
            secondarySegmentYPositions = new float[segmentCount];

            cachedDensities = new float[segmentCount];
            float x = SEGMENT_WIDTH_HALF;
            for (int i = 0; i < segmentYPositions.length; ++i, x += SEGMENT_WIDTH) {
                float density = densityProvider.getDensityAt(x / cachedCanvasWidth);
                cachedDensities[i] = MathUtils.clamp(density, 0f, 1f);
            }
        }

        if (cachedCanvasHeight != canvas.getHeight() || wasDurationChanged) {
            wasDurationChanged = false;
            cachedCanvasHeight = canvas.getHeight();
            float fCanvasH = (float) cachedCanvasHeight;
            if (duration == 0) return;
            this.p1 = fCanvasH / duration;
            this.p2 = fCanvasH / (duration * MIN_DURATION_RATE_PRIMARY) - p1;
            this.s1 = fCanvasH / (duration * DURATION_RATE_SECONDARY);
            this.s2 =  fCanvasH / (duration * MIN_DURATION_RATE_SECONDARY) - s1;
        }
    }

    // Used in pathJoinSegmentsSmoothly().
    private static final float DRAWING_METHOD_THRESHOLD = 1f;

    private static void pathJoinSegmentsSmoothly(Path path, float[] segmentYPositions,
                                                 float startX, float startY) {

        if (segmentYPositions.length == 0) return;
        path.moveTo(startX, startY);
        path.rQuadTo(0, segmentYPositions[0], SEGMENT_WIDTH_HALF, segmentYPositions[0]);

        for (int i = 0; i < segmentYPositions.length - 1; ++i) {
            float heightDiff = segmentYPositions[i + 1] - segmentYPositions[i];
            if (heightDiff != 0.f) {
                // fixme:: Use different drawing method depending on the drawing's shape.
                // The method1 is powerful when draw extended lumps, and it is
                // better to use the method2 when draw not-extended lump.
                if (Math.abs(heightDiff / segmentYPositions[i + 1]) > DRAWING_METHOD_THRESHOLD) {
                    // METHOD1 :: Use two 2d-bezier curve.
                    float hDiffHalf = heightDiff / 2;
                    path.rQuadTo(SEGMENT_WIDTH_HALF, 0, SEGMENT_WIDTH_HALF, hDiffHalf);
                    path.rQuadTo(0, hDiffHalf, SEGMENT_WIDTH_HALF, hDiffHalf);

                } else {
                    // METHOD2 :: Use one 3d-bezier curve.
                    path.rCubicTo(SEGMENT_WIDTH_HALF, 0,
                            SEGMENT_WIDTH_HALF, heightDiff,
                            SEGMENT_WIDTH, heightDiff);
                }

            } else {
                path.rLineTo(SEGMENT_WIDTH, 0);
            }
        }

        path.rQuadTo(SEGMENT_WIDTH_HALF, 0,
                SEGMENT_WIDTH_HALF, -segmentYPositions[segmentYPositions.length - 1]);
    }

    private static class MeltingAnimation extends Animation {

        final MeltingView view;
        final boolean reverse;
        float prevTime = 0f;

        MeltingAnimation(MeltingView view, boolean reverse) {
            this(view, new LinearInterpolator(), reverse);
        }

        MeltingAnimation(MeltingView view, android.view.animation.Interpolator interpolator, boolean reverse) {
            this.view = view;
            this.reverse = reverse;
            this.setInterpolator(interpolator);
            this.setDuration(view.getDuration());
            this.setAnimationListener(view.getAnimationListener());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (reverse) {
                view.addProgress(prevTime - interpolatedTime);
            } else {
                view.addProgress(interpolatedTime - prevTime);
            }
            view.invalidate();
            prevTime = interpolatedTime;
        }
    }

    abstract public static class DensityDistributionProvider {
        /**
         *
         * @param x 0f ~ 1f (= positionX / width)
         * @return 0f ~ 1f
         */
        abstract public float getDensityAt(float x);
    }

    private static final class DefaultDensityDistProvider extends
            MeltingView.DensityDistributionProvider {
        @Override
        public float getDensityAt(float x) {
            return 0.5f + (float) (0.5f * Math.sin(2*Math.PI*x));
        }
    }

    public static final class SideToCenterDensityDistProvider
            extends MeltingView.DensityDistributionProvider {
        @Override
        public float getDensityAt(float x) {
            return 4 * (x - 0.5f) * (x - 0.5f);
        }
    }
}