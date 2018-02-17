package com.chabiio.potecola.ui.behavior;

/**
 * Created by lionm on 2/17/2018.
 */

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.chabiio.potecola.R;

import java.lang.ref.WeakReference;

/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as
 * a bottom sheet.
 */
public class RLSheetBehavior<V extends View> extends AbsSheetBehavior<V> {

    /**
     * Peek at the 16:9 ratio keyline of its parent.
     *
     * <p>This can be used as a parameter for {@link #setPeekWidth(int)}.
     * {@link #getPeekWidth()} will return this when the value is set.</p>
     */
    public static final int PEEK_WIDTH_AUTO = -1;
    private static final float HIDE_THRESHOLD = 0.5f;
    private static final float HIDE_FRICTION = 0.1f;
    private float mMaximumVelocity;
    private int mPeekWidth;
    private boolean mPeekWidthAuto;
    private int mPeekWidthMin;
    private int mMinOffset;
    private int mMaxOffset;
    private boolean mHideable;
    private boolean mSkipCollapsed;
    @State private int mState = STATE_COLLAPSED;
    private ViewDragHelper mViewDragHelper;
    private boolean mIgnoreEvents;
    private int mLastNestedScrollDx;
    private boolean mNestedScrolled;
    private int mParentWidth;
    private WeakReference<V> mViewRef;
    private WeakReference<View> mNestedScrollingChildRef;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId;
    private int mInitialX;
    private boolean mTouchingScrollingChild;
    private boolean mIsLeftHandedMode = false;

    /**
     * Default constructor for instantiating RLSheetBehaviors.
     */
    public RLSheetBehavior() {
    }
    /**
     * Default constructor for inflating RLSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public RLSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SheetBehavior_Layout);
        TypedValue value = a.peekValue(R.styleable.SheetBehavior_Layout_behavior_sheet_peekSize);
        if (value != null && value.data == PEEK_WIDTH_AUTO) {
            setPeekWidth(value.data);
        } else {
            setPeekWidth(a.getDimensionPixelSize(
                    R.styleable.SheetBehavior_Layout_behavior_sheet_peekSize, PEEK_WIDTH_AUTO));
        }
        setHideable(a.getBoolean(R.styleable.SheetBehavior_Layout_behavior_sheet_hideable, false));
        setSkipCollapsed(a.getBoolean(R.styleable.SheetBehavior_Layout_behavior_sheet_skipCollapsed, false));
        mIsLeftHandedMode = a.getBoolean(R.styleable.SheetBehavior_Layout_behavior_sheet_leftHanded, false);
        a.recycle();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
        return new SavedState(super.onSaveInstanceState(parent, child), mState, mIsLeftHandedMode);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        mIsLeftHandedMode = ss.isLeftHandedMode;
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            ViewCompat.setFitsSystemWindows(child, true);
        }
        int savedLeft = child.getLeft();
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection);
        // Offset the bottom sheet
        mParentWidth = parent.getWidth();
        int peekWidth;
        if (mPeekWidthAuto) {
            if (mPeekWidthMin == 0) {
                mPeekWidthMin = parent.getResources().getDimensionPixelSize(R.dimen.rl_sheet_peek_width_min);
            }
            peekWidth = Math.max(mPeekWidthMin, mParentWidth - parent.getHeight() * 9 / 16);
        } else {
            peekWidth = mPeekWidth;
        }

        if (mIsLeftHandedMode) {
            final int childWidth = child.getWidth();
            mMaxOffset = Math.min(0, mParentWidth - childWidth); // expanded
            mMinOffset = Math.min(peekWidth - childWidth, mMaxOffset); // collapsed
        } else {
            mMinOffset = Math.max(0, mParentWidth - child.getWidth());
            mMaxOffset = Math.max(mParentWidth - peekWidth, mMinOffset);
        }

        if (mState == STATE_EXPANDED) {
            ViewCompat.offsetLeftAndRight(child, mIsLeftHandedMode ? mMaxOffset : mMinOffset);
        } else if (mHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetLeftAndRight(child, mIsLeftHandedMode ? -child.getHeight() : mParentWidth);
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetLeftAndRight(child, mIsLeftHandedMode ? mMinOffset : mMaxOffset);
        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
            ViewCompat.offsetLeftAndRight(child, savedLeft - child.getLeft());
        }

        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!child.isShown()) {
            mIgnoreEvents = true;
            return false;
        }
        int action = event.getActionMasked();
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchingScrollingChild = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mInitialX = (int) event.getX();
                int initialY = (int) event.getY();
                View scroll = mNestedScrollingChildRef != null
                        ? mNestedScrollingChildRef.get() : null;
                if (scroll != null && parent.isPointInChildBounds(scroll, mInitialX, initialY)) {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
                        !parent.isPointInChildBounds(child, mInitialX, initialY);
                break;
        }
        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
        return action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) &&
                Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop();
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!child.isShown()) {
            return false;
        }
        int action = event.getActionMasked();
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (mViewDragHelper != null) {
            mViewDragHelper.processTouchEvent(event);
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop()) {
                mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
            }
        }
        return !mIgnoreEvents;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        mLastNestedScrollDx = 0;
        mNestedScrolled = false;
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx,
                                  int dy, int[] consumed) {

        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild) {
            return;
        }
        int currentLeft = child.getLeft();
        int newLeft = currentLeft - dx;
        if (dx > 0) { // Upward
            if (mIsLeftHandedMode) {
                if (mState != STATE_EXPANDED || !target.canScrollHorizontally(1)) {
                    if (mMinOffset <= newLeft || mHideable) {
                        consumed[0] = dx;
                        ViewCompat.offsetLeftAndRight(child, -dx);
                        setStateInternal(STATE_DRAGGING);
                    } else {
                        consumed[0] = currentLeft - mMinOffset;
                        ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                        setStateInternal(STATE_COLLAPSED);
                    }
                }
            } else {
                if (newLeft < mMinOffset) {
                    consumed[0] = currentLeft - mMinOffset;
                    ViewCompat.offsetTopAndBottom(child, -consumed[0]);
                    setStateInternal(STATE_EXPANDED);
                } else {
                    consumed[0] = dx;
                    ViewCompat.offsetTopAndBottom(child, -dx);
                    setStateInternal(STATE_DRAGGING);
                }
            }

        } else if (dx < 0) { // Downward
            if (mIsLeftHandedMode) {
                if (mMaxOffset < newLeft) {
                    consumed[0] = currentLeft - mMaxOffset;
                    ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                    setStateInternal(STATE_EXPANDED);
                } else {
                    consumed[0] = dx;
                    ViewCompat.offsetLeftAndRight(child, -dx);
                    setStateInternal(STATE_DRAGGING);
                }

            } else {
                if (!target.canScrollHorizontally(-1)) {
                    if (newLeft <= mMaxOffset || mHideable) {
                        consumed[0] = dx;
                        ViewCompat.offsetTopAndBottom(child, -dx);
                        setStateInternal(STATE_DRAGGING);
                    } else {
                        consumed[0] = currentLeft - mMaxOffset;
                        ViewCompat.offsetTopAndBottom(child, -consumed[0]);
                        setStateInternal(STATE_COLLAPSED);
                    }
                }
            }
        }
        dispatchOnSlide(child.getLeft());
        mLastNestedScrollDx = dx;
        mNestedScrolled = true;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
        if ((mIsLeftHandedMode && child.getLeft() == mMaxOffset) ||
                (!mIsLeftHandedMode && child.getLeft() == mMinOffset)) {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        if (mNestedScrollingChildRef == null ||
                target != mNestedScrollingChildRef.get() || !mNestedScrolled) {
            return;
        }
        int left;
        int targetState;

        if (mIsLeftHandedMode) {
            if (mLastNestedScrollDx < 0) {
                left = mMaxOffset;
                targetState = STATE_EXPANDED;
            } else if (mHideable && shouldHide(child, getXVelocity())) {
                left = -child.getWidth();
                targetState = STATE_HIDDEN;
            } else if (mLastNestedScrollDx == 0) {
                int currentTop = child.getTop();
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                    left = mMinOffset;
                    targetState = STATE_COLLAPSED;
                } else {
                    left = mMaxOffset;
                    targetState = STATE_EXPANDED;
                }
            } else {
                left = mMinOffset;
                targetState = STATE_COLLAPSED;
            }

        } else {
            if (mLastNestedScrollDx > 0) {
                left = mMinOffset;
                targetState = STATE_EXPANDED;
            } else if (mHideable && shouldHide(child, getXVelocity())) {
                left = mParentWidth;
                targetState = STATE_HIDDEN;
            } else if (mLastNestedScrollDx == 0) {
                int currentLeft = child.getLeft();
                if (Math.abs(currentLeft - mMinOffset) < Math.abs(currentLeft - mMaxOffset)) {
                    left = mMinOffset;
                    targetState = STATE_EXPANDED;
                } else {
                    left = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
            } else {
                left = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
        }

        if (mViewDragHelper.smoothSlideViewTo(child, left, child.getTop())) {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
        } else {
            setStateInternal(targetState);
        }
        mNestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
                                    float velocityX, float velocityY) {
        return target == mNestedScrollingChildRef.get() &&
                (mState != STATE_EXPANDED ||
                        super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekWidth The height of the collapsed bottom sheet in pixels, or
     *                   {@link #PEEK_WIDTH_AUTO} to configure the sheet to peek automatically
     *                   at 16:9 ratio keyline.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final void setPeekWidth(int peekWidth) {
        boolean layout = false;
        if (peekWidth == PEEK_WIDTH_AUTO) {
            if (!mPeekWidthAuto) {
                mPeekWidthAuto = true;
                layout = true;
            }
        } else if (mPeekWidthAuto || mPeekWidth != peekWidth) {
            mPeekWidthAuto = false;
            mPeekWidth = Math.max(0, peekWidth);
            if (mIsLeftHandedMode) {
                mMinOffset = (mViewRef != null && mViewRef.get() != null)
                        ? peekWidth - mViewRef.get().getWidth() : mMaxOffset;
            } else {
                mMaxOffset = mParentWidth - peekWidth;
            }
            layout = true;
        }
        if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
            V view = mViewRef.get();
            if (view != null) {
                view.requestLayout();
            }
        }
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_WIDTH_AUTO}
     *         if the sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final int getPeekWidth() {
        return mPeekWidthAuto ? PEEK_WIDTH_AUTO : mPeekWidth;
    }

    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public void setHideable(boolean hideable) {
        mHideable = hideable;
    }

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public boolean isHideable() {
        return mHideable;
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public void setSkipCollapsed(boolean skipCollapsed) {
        mSkipCollapsed = skipCollapsed;
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public boolean getSkipCollapsed() {
        return mSkipCollapsed;
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, or
     *              {@link #STATE_HIDDEN}.
     */
    public final void setState(final @State int state) {
        if (state == mState) {
            return;
        }
        if (mViewRef == null) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED ||
                    (mHideable && state == STATE_HIDDEN)) {
                mState = state;
            }
            return;
        }
        final V child = mViewRef.get();
        if (child == null) {
            return;
        }
        // Start the animation; wait until a pending layout if there is one.
        ViewParent parent = child.getParent();
        if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child)) {
            child.post(new Runnable() {
                @Override
                public void run() {
                    startSettlingAnimation(child, state);
                }
            });
        } else {
            startSettlingAnimation(child, state);
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_COLLAPSED}, {@link #STATE_DRAGGING},
     * {@link #STATE_SETTLING}, and {@link #STATE_HIDDEN}.
     */
    @State
    public final int getState() {
        return mState;
    }

    public final boolean isLeftHandedMode() {
        return mIsLeftHandedMode;
    }

    private void setStateInternal(@State int state) {
        if (mState == state) {
            return;
        }
        mState = state;
        View sheet = mViewRef.get();
        if (sheet != null && getCallback() != null) {
            getCallback().onStateChanged(sheet, state);
        }
    }

    private void reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean shouldHide(View child, float xvel) {
        if (mSkipCollapsed) {
            return true;
        }

        if (mIsLeftHandedMode) {
            if (mMinOffset + child.getWidth() < child.getRight()) {
                // It should not hide, but collapse.
                return false;
            }
            final float newLeft = child.getLeft() + xvel * HIDE_FRICTION;
            return Math.abs(newLeft - mMinOffset) / (float) mPeekWidth > HIDE_THRESHOLD;

        } else {
            if (child.getLeft() < mMaxOffset) {
                // It should not hide, but collapse.
                return false;
            }
            final float newLeft = child.getLeft() + xvel * HIDE_FRICTION;
            return Math.abs(newLeft - mMaxOffset) / (float) mPeekWidth > HIDE_THRESHOLD;
        }
    }

    @VisibleForTesting
    View findScrollingChild(View view) {
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    private float getXVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        return mVelocityTracker.getXVelocity(mActivePointerId);
    }

    private void startSettlingAnimation(View child, int state) {
        int left;
        if (state == STATE_COLLAPSED) {
            left = mIsLeftHandedMode ? mMinOffset : mMaxOffset;
        } else if (state == STATE_EXPANDED) {
            left = mIsLeftHandedMode ? mMaxOffset : mMinOffset;
        } else if (mHideable && state == STATE_HIDDEN) {
            left = mIsLeftHandedMode ? -child.getWidth() : mParentWidth;
        } else {
            throw new IllegalArgumentException("Illegal state argument: " + state);
        }
        if (mViewDragHelper.smoothSlideViewTo(child, left, child.getTop())) {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
        } else {
            setStateInternal(state);
        }
    }

    private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (mState == STATE_DRAGGING) {
                return false;
            }
            if (mTouchingScrollingChild) {
                return false;
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && scroll.canScrollHorizontally(-1)) {
                    // Let the content scroll up
                    return false;
                }
            }
            return mViewRef != null && mViewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            dispatchOnSlide(left);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int left;
            @State int targetState;
            if (mIsLeftHandedMode) {
                if (0 < xvel) {
                    left = mMaxOffset;
                    targetState = STATE_EXPANDED;
                } else if (mHideable && shouldHide(releasedChild, xvel)) {
                    left = (mViewRef != null && mViewRef.get() != null)
                            ? -mViewRef.get().getWidth() : -mParentWidth;
                    targetState = STATE_HIDDEN;
                } else if (yvel == 0.f) {
                    int currentLeft = releasedChild.getLeft();
                    if (Math.abs(currentLeft - mMinOffset) < Math.abs(currentLeft - mMaxOffset)) {
                        left = mMinOffset;
                        targetState = STATE_COLLAPSED;
                    } else {
                        left = mMaxOffset;
                        targetState = STATE_EXPANDED;
                    }
                } else {
                    left = mMinOffset;
                    targetState = STATE_COLLAPSED;
                }

            } else {
                if (xvel < 0) {
                    left = mMinOffset;
                    targetState = STATE_EXPANDED;
                } else if (mHideable && shouldHide(releasedChild, xvel)) {
                    left = mParentWidth;
                    targetState = STATE_HIDDEN;
                } else if (xvel == 0.f) {
                    int currentLeft = releasedChild.getLeft();
                    if (Math.abs(currentLeft - mMinOffset) < Math.abs(currentLeft - mMaxOffset)) {
                        left = mMinOffset;
                        targetState = STATE_EXPANDED;
                    } else {
                        left = mMaxOffset;
                        targetState = STATE_COLLAPSED;
                    }
                } else {
                    left = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
            }

            if (mViewDragHelper.settleCapturedViewAt(left, releasedChild.getTop())) {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
            } else {
                setStateInternal(targetState);
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return mIsLeftHandedMode
                    ? MathUtils.clamp(left, mHideable ? -child.getWidth() : mMinOffset, mMaxOffset)
                    : MathUtils.clamp(left, mMinOffset, mHideable ? mParentWidth : mMaxOffset);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (mHideable) {
                return mIsLeftHandedMode
                        ? Math.abs(mMaxOffset + child.getHeight())
                        : mParentWidth - mMinOffset;
            } else {
                return mIsLeftHandedMode
                        ? Math.abs(mMaxOffset - mMinOffset)
                        : mMaxOffset - mMinOffset;
            }
        }
    };

    private void dispatchOnSlide(int left) {
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null && getCallback() != null) {
            if (mIsLeftHandedMode) {
                if (left < mMinOffset) {
                    getCallback().onSlide(bottomSheet,
                            (float) (left - mMinOffset) / (bottomSheet.getWidth() + mMinOffset));
                } else {
                    getCallback().onSlide(bottomSheet,
                            (float) (left - mMinOffset) / ((mMaxOffset - mMinOffset)));
                }

            } else {
                if (left > mMaxOffset) {
                    getCallback().onSlide(bottomSheet, (float) (mMaxOffset - left) /
                            (mParentWidth - mMaxOffset));
                } else {
                    getCallback().onSlide(bottomSheet,
                            (float) (mMaxOffset - left) / ((mMaxOffset - mMinOffset)));
                }
            }
        }
    }

    private class SettleRunnable implements Runnable {

        private final View mView;
        @State private final int mTargetState;

        SettleRunnable(View view, @State int targetState) {
            mView = view;
            mTargetState = targetState;
        }

        @Override
        public void run() {
            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this);
            } else {
                setStateInternal(mTargetState);
            }
        }
    }

    protected static class SavedState extends AbsSavedState {

        @State final int state;
        final boolean isLeftHandedMode;

        public SavedState(Parcel source) {
            this(source, null);
        }

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            //noinspection ResourceType
            state = source.readInt();
            isLeftHandedMode = source.readInt() !=0;
        }

        public SavedState(Parcelable superState, @State int state, boolean isLeftHandedMode) {
            super(superState);
            this.state = state;
            this.isLeftHandedMode = isLeftHandedMode;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
            out.writeInt(isLeftHandedMode ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * A utility function to get the {@link RLSheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link RLSheetBehavior}.
     * @return The {@link RLSheetBehavior} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> RLSheetBehavior<V> from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof RLSheetBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with RLSheetBehavior");
        }
        return (RLSheetBehavior<V>) behavior;
    }
}
