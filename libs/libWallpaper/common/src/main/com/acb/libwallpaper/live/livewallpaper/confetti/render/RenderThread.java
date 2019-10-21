package com.acb.libwallpaper.live.livewallpaper.confetti.render;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Denotes that the annotated method should only be called on the OpenGL render thread.
 * If the annotated element is a class, then all methods in the class should be called
 * on the OpenGL render thread.
 * <p>
 * Example:
 * <pre><code>
 *  &#64;RenderThread
 *  public void onDrawFrame(GL10 gl) { ... }
 * </code></pre>
 */
@Documented
@Retention(CLASS)
@Target({METHOD,CONSTRUCTOR,TYPE})
public @interface RenderThread {
}
