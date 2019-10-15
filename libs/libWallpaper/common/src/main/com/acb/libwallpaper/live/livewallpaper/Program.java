package com.acb.libwallpaper.live.livewallpaper;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;

import com.ihs.app.framework.HSApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Program {

    private static final Pattern PATTERN_INCLUDE_HEADER = Pattern.compile(
			".*#include\\s+[\"<]([^\">]+)*[\">]");

	private static List<String> infoLogs = new ArrayList<>();

	static List<String> getInfoLogs() {
		return infoLogs;
	}

	public static int loadProgram(
			String vertexShader,
			String fragmentShader) {
		int vs, fs, p = 0;
		infoLogs.clear();

		// 1. compile vertex shader
		if ((vs = compileShader(
				GLES20.GL_VERTEX_SHADER,
				vertexShader)) != 0) {

			// 2. pre-process fragment shader
			try {
				fragmentShader = preProcessFragmentShader(fragmentShader);
			} catch (ShaderPreProcessError e) {
				infoLogs.add(e.toString());
				return p;
			}

			// 3. compile fragment shader
			if ((fs = compileShader(
					GLES20.GL_FRAGMENT_SHADER,
					fragmentShader)) != 0) {
				p = linkProgram(vs, fs);

				// mark shader objects as deleted so they get
				// deleted as soon as glDeleteProgram() does
				// detach them
				GLES20.glDeleteShader(fs);
			}
		}

		// same as above
		GLES20.glDeleteShader(vs);

		return p;
	}

	private static int linkProgram(int... shaders) {
		int p = GLES20.glCreateProgram();

		if (p == 0) {
			infoLogs.add("Cannot create program");
			return 0;
		}

		for (int shader : shaders) {
			GLES20.glAttachShader(p, shader);
		}

		GLES20.glLinkProgram(p);

		int linkStatus[] = new int[1];
		GLES20.glGetProgramiv(
				p,
				GLES20.GL_LINK_STATUS,
				linkStatus, 0);

		infoLogs.add(GLES20.glGetProgramInfoLog(p));

		if (linkStatus[0] != GLES20.GL_TRUE) {
			GLES20.glDeleteProgram(p);
			p = 0;
		}

		return p;
	}

	private static int compileShader(int type, String src) {
		int s = GLES20.glCreateShader(type);

		if (s == 0) {
			infoLogs.add("Cannot create shader");
			return 0;
		}

		GLES20.glShaderSource(s, src);
		GLES20.glCompileShader(s);

		int compiled[] = new int[1];
		GLES20.glGetShaderiv(
				s,
				GLES20.GL_COMPILE_STATUS,
				compiled,
				0);

		infoLogs.add(GLES20.glGetShaderInfoLog(s));

		if (compiled[0] != GLES20.GL_TRUE) {
			GLES20.glDeleteShader(s);
			s = 0;
		}

		return s;
	}

	/**
	 * Perform pre-precessing on fragment shader: process "#include" headers and including them
	 * in a single shader source.
	 */
    private static String preProcessFragmentShader(String src) throws ShaderPreProcessError {
        return preProcessFragmentShaderInternal(src, new ArrayList<>());
    }

	private static String preProcessFragmentShaderInternal(
	        String src,
            List<String> infiniteLoopBreaker) throws ShaderPreProcessError {
		Context context = HSApplication.getContext();
        while (true) {
            Matcher m = PATTERN_INCLUDE_HEADER.matcher(src);
            if (!m.find()) {
                // No #include
                break;
            }
            String includedItem = m.group(1);

            // Check for infinite loop: A -> B -> C -> A -> ...
            // Note that duplicated inclusion also breaks the rule. But it will break
            // shader compilation any way, so we don't have to check for that here.
            if (infiniteLoopBreaker.contains(includedItem)) {
                throw new IncludeHasInfiniteLoopError(includedItem);
            }

            String expanded;
            try {
                expanded = loadShaderResource(context, includedItem);
            } catch (IOException e) {
                throw new ShaderPreProcessError("Failed to load resource " + includedItem);
            }
            if (expanded != null) {
                infiniteLoopBreaker.add(includedItem);

                // Recursively expand
                String recursivelyExpanded = preProcessFragmentShaderInternal(
                        expanded, infiniteLoopBreaker);
                src = m.replaceFirst(recursivelyExpanded);
            } else {
                throw new IncludeNoSuchFileError(includedItem);
            }
        }
		return src;
	}

	public static String loadShaderResource(Context context, String name) throws IOException {
		AssetManager am = context.getAssets();
		InputStream is = am.open(LiveWallpaperConsts.DIRECTORY + name);
		if (is == null) {
			return null;
		}

		int l = is.available();
		byte b[] = new byte[l];
		return is.read(b) == l ? new String(b, "UTF-8") : null;
	}

	private static class ShaderPreProcessError extends Exception {
		ShaderPreProcessError(String message) {
			super(message);
		}
	}

	private static class IncludeNoSuchFileError extends ShaderPreProcessError {
		IncludeNoSuchFileError(String noSuchFileName) {
			super("#include<> a file that does not exist in raw resource directory: " + noSuchFileName);
		}
	}

	private static class IncludeHasInfiniteLoopError extends ShaderPreProcessError {
		IncludeHasInfiniteLoopError(String oneFileNameInLoop) {
			super("Detected infinite loop in #include<>, one file in loop is " + oneFileNameInLoop);
		}
	}
}
