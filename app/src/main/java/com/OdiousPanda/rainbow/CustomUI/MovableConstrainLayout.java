package com.OdiousPanda.rainbow.CustomUI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MovableConstrainLayout extends ConstraintLayout implements View.OnTouchListener {
    public static final int SNAP_DURATION = 150;
    private float downRawY;
    private float dY;
    private OnPositionChangedCallback callback;

    public MovableConstrainLayout(Context context) {
        super(context);
        init();
    }

    public MovableConstrainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableConstrainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    public void setCallback(OnPositionChangedCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ConstraintLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int viewHeight = view.getHeight();
        View viewParent = (View) view.getParent();
        int parentHeight = viewParent.getHeight();
        int action = motionEvent.getAction();
        float bottomBoundary = (float) viewHeight * 58 / 100;
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d("movable layout", "onTouch: action_down");
            downRawY = motionEvent.getRawY();
            dY = view.getY() - downRawY;
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            Log.d("movable layout", "onTouch: action_move");
            float newY = motionEvent.getRawY() + dY;
            newY = Math.max((float) parentHeight / 2, newY); // Don't allow the layout past the top of the parent /2
            newY = Math.min(parentHeight - bottomBoundary - layoutParams.bottomMargin, newY); // Don't allow the layout past the bottom of the parent
            view.animate()
                    .y(newY)
                    .setDuration(0)
                    .start();
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            Log.d("movable layout", "onTouch: action_up");
            float upRawY = motionEvent.getRawY();
            float upDY = upRawY - downRawY;
            Log.d("movable layout", "onTouch: " + upDY);
            float restY = (float) parentHeight / 2;
            if (Math.abs(upDY) < bottomBoundary / 2) {
                if (upRawY > (float) parentHeight / 2 + bottomBoundary / 2) {
                    restY = parentHeight - bottomBoundary - layoutParams.bottomMargin;
                }
            } else {
                if (upDY > 0) {
                    restY = parentHeight - bottomBoundary - layoutParams.bottomMargin;
                }
            }
            view.animate()
                    .y(restY)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(SNAP_DURATION)
                    .start();
            callback.onMoved(restY);
        }

        return super.onTouchEvent(motionEvent);
    }

    public interface OnPositionChangedCallback {
        void onMoved(float y);
    }
}
