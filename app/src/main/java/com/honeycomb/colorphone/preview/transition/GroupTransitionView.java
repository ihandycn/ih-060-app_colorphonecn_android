package com.honeycomb.colorphone.preview.transition;

import java.util.ArrayList;
import java.util.List;

public class GroupTransitionView implements TransitionView {
    private List<TransitionView> mTransitionViews = new ArrayList<>(2);

    public GroupTransitionView() {
    }

    public void addTranstionView(TransitionView transitionView) {
        mTransitionViews.add(transitionView);
    }

    @Override
    public void show(boolean anim) {
        for (TransitionView transitionView : mTransitionViews) {
            transitionView.show(anim);
        }
    }

    @Override
    public void hide(boolean anim) {
        for (TransitionView transitionView : mTransitionViews) {
            transitionView.hide(anim);
        }
    }
}