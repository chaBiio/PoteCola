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
import android.support.annotation.IntDef;
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lionm on 2/24/2018.
 */

public class MeltingView extends View {

    public static final int GRAVITY_TOP_DOWN = 1;
    public static final int GRAVITY_BOTTOM_UP = 2;
    public static final int GRAVITY_LEFT_TO_RIGHT = 3;
    public static final int GRAVITY_RIGHT_TO_LEFT = 4;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GRAVITY_TOP_DOWN, GRAVITY_BOTTOM_UP, GRAVITY_LEFT_TO_RIGHT, GRAVITY_RIGHT_TO_LEFT})
    public @interface Gravity {}

    @Px private static final int DEFAULT_SEGMENT_WIDTH = 140;
    @ColorInt private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    @Gravity private static final int DEFAULT_GRAVITY = GRAVITY_TOP_DOWN;
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
    @Gravity private int gravity = DEFAULT_GRAVITY;
    @Px private int segmentWidth = DEFAULT_SEGMENT_WIDTH;
    @Px private int segmentWidthHalf = segmentWidth / 2;

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
            gravity = ta.getInteger(R.styleable.MeltingView_melting_gravity, DEFAULT_GRAVITY);
            primaryColor = ta.getColor(R.styleable.MeltingView_melting_primary_color, fetchAccentColor(context));
            secondaryColor = ta.getColor(R.styleable.MeltingView_melting_secondary_color, DEFAULT_PRIMARY_COLOR);
            duration = ta.getInteger(R.styleable.MeltingView_melting_animation_duration, DEFAULT_DURATION);
            disablePreDrawing = ta.getBoolean(R.styleable.MeltingView_melting_disable_pre_drawing, false);
            setProgress(ta.getFloat(R.styleable.MeltingView_melting_progress, DEFAULT_PROGRESS));
            setSegmentWidth(ta.getDimensionPixelSize(R.styleable.MeltingView_melting_segment_width, DEFAULT_SEGMENT_WIDTH), false);
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

    public void setGravity(@Gravity int gravity, boolean forceUpdate) {
        if (this.gravity != gravity) {
            this.gravity = gravity;
            wasGravityChanged = true;
            if (forceUpdate) invalidate();
        }
    }

    @Gravity
    public int getGravity() {
        return gravity;
    }

    public void setSegmentWidth(@Px int px, boolean forceUpdate) {
        if (segmentWidth != px) {
            segmentWidth = Math.max(1, px);
            segmentWidthHalf = segmentWidth / 2;
            wasSegmentWidthChanged = true;
            if (forceUpdate) invalidate();
        }
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
    @Px private static final int EXTRA_DRAWABLE_REGION_SIZE = 20;
    //    @Px private static final int SEGMENT_WIDTH = 120;
//    @Px private static final int SEGMENT_WIDTH_HALF = SEGMENT_WIDTH / 2;

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
        // Calculate the coordinates assuming that 'gravity' is 'GRAVITY_TOP_DOWN'.
        // Then, rotate or translate the canvas if needed.
        super.onDraw(canvas);
        float canvasW;
        if (gravity == GRAVITY_TOP_DOWN || gravity == GRAVITY_BOTTOM_UP) {
             canvasW = canvas.getWidth() + EXTRA_DRAWABLE_REGION_SIZE;
            prepareForDrawing((int) canvasW, canvas.getHeight());
        } else {
            canvasW = canvas.getHeight() + EXTRA_DRAWABLE_REGION_SIZE;
            prepareForDrawing((int) canvasW, canvas.getWidth());
        }

        if (cachedDensities.length == 0) return;
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

        if (gravity != GRAVITY_TOP_DOWN) {
            if (gravity == GRAVITY_BOTTOM_UP) {
                canvas.rotate(180, canvas.getWidth()/2, canvas.getHeight()/2);
            } else if (gravity == GRAVITY_LEFT_TO_RIGHT) {
                canvas.rotate(-90, 0, 0);
                canvas.translate(-canvas.getHeight(), 0);
            } else {
                // gravity is GRAVITY_RIGHT_TO_LEFT
                canvas.rotate(90, 0, 0);
                canvas.translate(0, -canvas.getWidth());
            }
        }

        // Draw the background.
        canvas.drawColor(backgroundColor);

        // Draw the pre-melting.
        if (!disablePreDrawing) {
            paint.setColor(secondaryColor);
            // fixme: Add +10px to the rect to hide unnecessary border line which appears sometimes.
            canvas.drawRect(-EXTRA_DRAWABLE_REGION_SIZE, 0, canvasW, preBaselineY + 10, paint);
            path.reset();
            pathJoinSegmentsSmoothly(path, secondarySegmentYPositions, -EXTRA_DRAWABLE_REGION_SIZE, preBaselineY);
            canvas.drawPath(path, paint);
        }

        // Draw the melting.
        paint.setColor(primaryColor);
        // fixme: Add +10px to the rect to hide unnecessary border line which appears sometimes.
        canvas.drawRect(-EXTRA_DRAWABLE_REGION_SIZE, 0, canvasW, baselineY + 10, paint);path.reset();
        pathJoinSegmentsSmoothly(path, segmentYPositions, -EXTRA_DRAWABLE_REGION_SIZE, baselineY);
        canvas.drawPath(path, paint);
    }

    // Used in prepareForDrawing().
    private boolean wasSegmentWidthChanged = false;
    private boolean wasGravityChanged = false;
    private boolean wasDensityDistProviderChanged = false;
    private boolean wasDurationChanged = false;
    private int cachedCanvasWidth = -1;
    private int cachedCanvasHeight = -1;

    private void prepareForDrawing(int canvasW, int canvasH) {
        if (cachedCanvasWidth != canvasW ||
                wasDensityDistProviderChanged ||
                wasGravityChanged ||
                wasSegmentWidthChanged) {

            wasDensityDistProviderChanged = false;
            wasGravityChanged = false;
            wasSegmentWidthChanged = false;
            cachedCanvasWidth = canvasW;
            float availableWidth = cachedCanvasWidth + EXTRA_DRAWABLE_REGION_SIZE;
            int segmentCount = (int) Math.ceil(availableWidth / segmentWidth) + 1;
            segmentYPositions = new float[segmentCount];
            secondarySegmentYPositions = new float[segmentCount];

            cachedDensities = new float[segmentCount];
            float x = segmentWidthHalf;
            for (int i = 0; i < segmentYPositions.length; ++i, x += segmentWidth) {
                float density = densityProvider.getDensityAt(x / availableWidth);
                cachedDensities[i] = MathUtils.clamp(density, 0f, 1f);
            }
        }

        if (cachedCanvasHeight != canvasH || wasDurationChanged || wasGravityChanged) {
            wasDurationChanged = false;
            wasGravityChanged = false;
            cachedCanvasHeight = canvasH;
            float fCanvasH = (float) cachedCanvasHeight;
            if (duration == 0) return;
            this.p1 = fCanvasH / duration;
            this.p2 = fCanvasH / (duration * MIN_DURATION_RATE_PRIMARY) - p1;
            this.s1 = fCanvasH / (duration * DURATION_RATE_SECONDARY);
            this.s2 =  fCanvasH / (duration * MIN_DURATION_RATE_SECONDARY) - s1;
        }
    }

    // Used in pathJoinSegmentsSmoothly().
//    private static final float DRAWING_METHOD_THRESHOLD = 1f;

    private void pathJoinSegmentsSmoothly(Path path, float[] segmentYPositions,
                                                 float startX, float startY) {

        if (segmentYPositions.length == 0) return;
        path.moveTo(startX, startY);
        path.rQuadTo(0, segmentYPositions[0], segmentWidthHalf, segmentYPositions[0]);

        for (int i = 0; i < segmentYPositions.length - 1; ++i) {
            float heightDiff = segmentYPositions[i + 1] - segmentYPositions[i];
            if (heightDiff != 0.f) {
                // fixme:: Use different drawing method depending on the drawing's shape.
                // The method1 is powerful when draw extended lumps, and it is
                // better to use the method2 when draw not-extended lump.
//                if (Math.abs(heightDiff / segmentYPositions[i + 1]) > DRAWING_METHOD_THRESHOLD) {
//                    // METHOD1 :: Use two 2d-bezier curve.
//                    float hDiffHalf = heightDiff / 2;
//                    path.rQuadTo(segmentWidthHalf, 0, segmentWidthHalf, hDiffHalf);
//                    path.rQuadTo(0, hDiffHalf, segmentWidthHalf, hDiffHalf);
//
//                } else {
//                    // METHOD2 :: Use one 3d-bezier curve.
//                    path.rCubicTo(segmentWidthHalf, 0,
//                            segmentWidthHalf, heightDiff,
//                            segmentWidth, heightDiff);
//                }

                path.rCubicTo(segmentWidthHalf, 0,
                        segmentWidthHalf, heightDiff,
                        segmentWidth, heightDiff);

            } else {
                path.rLineTo(segmentWidth, 0);
            }
        }

        path.rQuadTo(segmentWidthHalf, 0,
                segmentWidthHalf, -segmentYPositions[segmentYPositions.length - 1]);
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
            double pix = Math.PI * x;
            return (float) (
                    0.2 +
                    0.13*Math.sin(pix) +
                    0.1*Math.sin(4*pix) +
                    0.1*Math.sin(10*(pix-Math.PI*0.15)));
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
