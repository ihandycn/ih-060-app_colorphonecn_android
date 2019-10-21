/**
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

import java.util.Random;

/**
 * The source from which confetti will appear. This can be either a line or a point.
 *
 * <p>Please note that the specified source represents the top left corner of the drawn
 * confetti. If you want the confetti to appear from off-screen, you'll have to offset it
 * with the confetti's size.
 */
public class ConfettiSource {

    private final int x0, y0, x1, y1;
    private final int dx0, dy0, dx1, dy1;

    /**
     * Specifies a point source from which all confetti will emit from.
     *
     * @param x x-coordinate of the point relative to the {@link android.opengl.GLSurfaceView}.
     * @param y y-coordinate of the point relative to the {@link android.opengl.GLSurfaceView}.
     */
    public ConfettiSource(int x, int y) {
        this(x, y, x, y);
    }

    /**
     * Specifies a line source from which all confetti will emit from.
     *
     * @param x0 x-coordinate of the first point relative to the {@link android.opengl.GLSurfaceView}.
     * @param y0 y-coordinate of the first point relative to the {@link android.opengl.GLSurfaceView}.
     * @param x1 x-coordinate of the second point relative to the {@link android.opengl.GLSurfaceView}.
     * @param y1 y-coordinate of the second point relative to the {@link android.opengl.GLSurfaceView}.
     */
    public ConfettiSource(int x0, int y0, int x1, int y1) {
        this(x0, 0, y0, 0, x1, 0, y1, 0);
    }

    /**
     * Specifies a line source from which all confetti will emit from.
     *
     * @param x0 x-coordinate of the first point relative to the {@link android.opengl.GLSurfaceView}.
     * @param y0 y-coordinate of the first point relative to the {@link android.opengl.GLSurfaceView}.
     * @param x1 x-coordinate of the second point relative to the {@link android.opengl.GLSurfaceView}.
     * @param y1 y-coordinate of the second point relative to the {@link android.opengl.GLSurfaceView}.
     */
    public ConfettiSource(int x0, int dx0, int y0, int dy0,
                          int x1, int dx1, int y1, int dy1) {
        this.x0 = x0;
        this.dx0 = dx0;
        this.y0 = y0;
        this.dy0 = dy0;
        this.x1 = x1;
        this.dx1 = dx1;
        this.y1 = y1;
        this.dy1 = dy1;
    }

    protected float getInitialX(Random random) {
        float thisX0 = ConfettiManager.getVarianceAmount(x0, dx0, random);
        float thisX1 = ConfettiManager.getVarianceAmount(x1, dx1, random);
        return thisX0 + (thisX1 - thisX0) * random.nextFloat();
    }

    protected float getInitialY(Random random) {
        float thisY0 = ConfettiManager.getVarianceAmount(y0, dy0, random);
        float thisY1 = ConfettiManager.getVarianceAmount(y1, dy1, random);
        return thisY0 + (thisY1 - thisY0) * random.nextFloat();
    }
}
