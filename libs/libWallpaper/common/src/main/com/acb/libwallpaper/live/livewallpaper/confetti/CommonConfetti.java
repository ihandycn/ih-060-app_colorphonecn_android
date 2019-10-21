/*
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acb.libwallpaper.live.livewallpaper.confetti;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.livewallpaper.confetti.confetto.BitmapConfetto;
import com.acb.libwallpaper.live.livewallpaper.confetti.render.ConfettiRenderer;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class CommonConfetti {

    private boolean isClickable;
    private boolean fromTouch;
    private ConfettiManager confettiManager;

    public CommonConfetti() {
    }

    public ConfettiManager getConfettiManager() {
        return confettiManager;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setFromTouch(boolean fromTouch) {
        this.fromTouch = fromTouch;
    }

    public boolean isFromTouch() {
        return fromTouch;
    }

    /**
     * Starts an infinite stream of confetti.
     *
     * @return the resulting {@link ConfettiManager} that's performing the animation.
     */
    public ConfettiManager infinite(float emissionRate) {
        return confettiManager
                .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
                .setEmissionRate(emissionRate)
                .animate();
    }

    public ConfettiManager infiniteSingle() {
        return confettiManager.setNumInitialCount(1)
                .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
                .setEmissionRate(0)
                .animate();
    }

    private ConfettoGenerator getGenerator(
            final @NonNull List<ConfettiRenderer.TextureRecord> textures,
            final long category, final int settingId) {
        List<ConfettiRenderer.TextureRecord> targets = new ArrayList<>();
        Stream.of(textures)
                .withoutNulls()
                .filter(texture -> ((texture.category == category && texture.settingId == settingId))
                        || (texture.category == LiveWallpaperConsts.COMMON && LiveWallpaperConsts.CLICK != category))
                .forEach(targets::add);

        if (targets.isEmpty()) {
            HSLog.e("textures must not be empty, category = " + category);
        }

        // Normalized the ratio
        final float ratios[] = new float[targets.size()];
        float sum = 0;
        for (int i = 0; i < targets.size(); i++) {
            ratios[i] = targets.get(i).ratio;
            sum += ratios[i];
        }
        for (int i = 0; i < targets.size(); i++) {
            ratios[i] /= sum;
            if (i > 0) {
                ratios[i] += ratios[i - 1];
            }
        }

        return random -> {
            float randomVal = random.nextFloat();
            int hit = -1;
            for (int i = 0; i < targets.size(); i++) {
                if (randomVal <= ratios[i]) {
                    hit = i;
                    break;
                }
            }
            return hit < 0 ? null : new BitmapConfetto(targets.get(hit));
        };
    }

    public void configureConfetti(@NonNull List<ConfettiRenderer.TextureRecord> textures,
                                  ConfettiFilter confettiFilter,
                                  ConfettiSource confettiSource,
                                  int width, int height, long category, int settingId) {
        final ConfettoGenerator generator = getGenerator(textures, category, settingId);

        confettiManager = new ConfettiManager(generator, confettiFilter,
                confettiSource, width, height);
    }
}
