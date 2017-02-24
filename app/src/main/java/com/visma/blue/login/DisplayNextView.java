package com.visma.blue.login;

import android.view.View;
import android.view.animation.Animation;

public final class DisplayNextView implements Animation.AnimationListener {
    private boolean mCurrentView;
    View image1;
    View image2;

    public DisplayNextView(boolean currentView, View image1, View image2) {
        mCurrentView = currentView;

        this.image1 = image1;
        this.image2 = image2;
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        image1.post(new SwapViews(mCurrentView, image1, image2));
    }

    public void onAnimationRepeat(Animation animation) {
    }
}