package com.acb.libwallpaper.live.livewallpaper.confetti;

import com.acb.libwallpaper.live.livewallpaper.confetti.confetto.Confetto;

/**
 * Filters confetti when generated.
 */
public interface ConfettiFilter {

    boolean filter(Confetto confetto);

    int maxIterationCount();
}
