package com.compelium.cpmplus.effects;

import java.lang.reflect.Method;

import com.tom.cpm.shared.util.Log;

/**
 * Asks the shader mod Iris whether it is drawing its shadows right now.
 *
 * With shaders on, Iris draws every entity a second time from the sun's point of view to
 * make the shadows. Particles must only come from the normal drawing. Iris is reached by
 * name through its public API, so this mod does not need Iris to be installed.
 */
public final class ShaderCompat {
	private static boolean tried;
	private static Object iris;
	private static Method shadowPass;

	private ShaderCompat() {
	}

	public static boolean isShadowPass() {
		if (!tried) init();
		Object api = iris;
		if (api == null) return false;
		try {
			return Boolean.TRUE.equals(shadowPass.invoke(api));
		} catch (Throwable e) {
			iris = null;
			Log.warn("[Compelium's CPM+] Could not ask Iris about its shadow pass; particles are still placed correctly, but your own may show in first person with shaders on", e);
			return false;
		}
	}

	private static void init() {
		tried = true;
		try {
			Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi", false, ShaderCompat.class.getClassLoader());
			Object instance = api.getMethod("getInstance").invoke(null);
			Method m = api.getMethod("isRenderingShadowPass");
			if (instance == null) return;
			shadowPass = m;
			iris = instance;
			Log.info("[Compelium's CPM+] Iris found: particles skip the shader shadow pass");
		} catch (ClassNotFoundException e) {
			// No Iris: nothing to do
		} catch (Throwable e) {
			Log.warn("[Compelium's CPM+] Iris found, but its API could not be read; particles are still placed correctly", e);
		}
	}
}
