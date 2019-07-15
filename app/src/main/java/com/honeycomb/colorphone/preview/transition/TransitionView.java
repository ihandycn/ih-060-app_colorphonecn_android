package com.honeycomb.colorphone.preview.transition;

public interface TransitionView {
    //TODO anim use Type-define, to use more animtion type.(FADE, TRANS...)
    void show(boolean anim);
    void hide(boolean anim);
}
