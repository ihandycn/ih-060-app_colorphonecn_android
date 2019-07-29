package com.honeycomb.colorphone.customize.livewallpaper.confetti;

import com.honeycomb.colorphone.customize.livewallpaper.confetti.confetto.Confetto;

/**
 * Filters confetti when generated.
 */
public interface ConfettiFilter {

    boolean filter(Confetto confetto);

    int maxIterationCount();
}
