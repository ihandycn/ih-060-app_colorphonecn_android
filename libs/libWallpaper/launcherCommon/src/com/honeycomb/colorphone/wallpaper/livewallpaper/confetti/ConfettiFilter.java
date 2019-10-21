package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti;


import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.confetto.Confetto;

/**
 * Filters confetti when generated.
 */
public interface ConfettiFilter {

    boolean filter(Confetto confetto);

    int maxIterationCount();
}
