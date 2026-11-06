package com.compelium.cpmplus.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tom.cpm.shared.animation.Animation;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationTrigger;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;
import com.tom.cpm.shared.util.Log;

/**
 * Reads a few private details of CPM's loaded models: which parts each named animation moves.
 * If a future CPM version renames these fields, the features that need them switch off
 * quietly and everything else keeps working.
 */
public final class CpmAccess {
	private static Field layerId, layerMask, valueId, components;
	private static boolean tried, ok;

	private CpmAccess() {
	}

	private static synchronized boolean init() {
		if (tried) return ok;
		tried = true;
		try {
			layerId = field(AnimationTrigger.LayerTrigger.class, "id");
			layerMask = field(AnimationTrigger.LayerTrigger.class, "mask");
			valueId = field(AnimationTrigger.ValueTrigger.class, "id");
			components = field(Animation.class, "componentIDs");
			ok = true;
		} catch (Throwable e) {
			Log.warn("[Compelium's CPM+] This CPM version is not supported for particle emitters", e);
			ok = false;
		}
		return ok;
	}

	private static Field field(Class<?> c, String name) throws NoSuchFieldException {
		Field f = c.getDeclaredField(name);
		f.setAccessible(true);
		return f;
	}

	/** The animations a named button (layer or value layer) plays. */
	public static List<IAnimation> animationsFor(AnimationRegistry reg, AbstractGestureButtonData btn) {
		if (!init()) return Collections.emptyList();
		List<IAnimation> out = new ArrayList<>();
		try {
			for (AnimationTrigger t : reg.getAnimations()) {
				if (btn instanceof BoolParameterToggleButtonData && t instanceof AnimationTrigger.LayerTrigger) {
					BoolParameterToggleButtonData b = (BoolParameterToggleButtonData) btn;
					if (layerId.getInt(t) == b.parameter && layerMask.getInt(t) == b.mask) out.addAll(t.animations);
				} else if (btn instanceof ValueParameterButtonData && t instanceof AnimationTrigger.ValueTrigger) {
					if (valueId.getInt(t) == ((ValueParameterButtonData) btn).parameter) out.addAll(t.animations);
				}
			}
		} catch (Throwable e) {
			Log.warn("[Compelium's CPM+] Could not read the model's animation triggers", e);
		}
		return out;
	}

	/** The parts an animation moves, or an empty array when it can't be read. */
	public static IModelComponent[] components(IAnimation anim) {
		if (!(anim instanceof Animation) || !init()) return new IModelComponent[0];
		try {
			IModelComponent[] c = (IModelComponent[]) components.get(anim);
			return c != null ? c : new IModelComponent[0];
		} catch (Throwable e) {
			return new IModelComponent[0];
		}
	}
}
