package com.compelium.cpmplus.physics;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.animation.AnimationEngine;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.anim.ParameterDetails;
import com.tom.cpm.shared.util.Log;

/**
 * Runs every time CPM prepares a player for drawing, right after CPM has filled
 * in that player's animation state.
 *
 * It writes this mod's trigger values straight into the copy of the animation values
 * CPM uses for that one frame. Nothing is sent over the network and nothing is stored
 * in CPM, so players without this mod simply see the model at rest.
 */
public final class PhysicsEngine {
	private static final Map<Player<?>, PlayerMotion> MOTION = new WeakHashMap<>();
	private static final int MAX_ERRORS = 20;
	private static int errors;
	private static boolean announced;

	private PhysicsEngine() {
	}

	public static void apply(Player<?> profile, AnimationState state) {
		if (errors >= MAX_ERRORS || profile == null || state == null) return;
		try {
			ModelDefinition def = profile.getModelDefinition();
			if (def == null) return;
			List<LayerBinding> bindings = ModelBindings.of(def);
			if (bindings.isEmpty()) return;

			AnimationEngine engine = MinecraftClientAccess.get().getPlayerRenderManager().getAnimationEngine();
			PlayerMotion motion = MOTION.get(profile);
			if (motion == null) {
				motion = new PlayerMotion();
				MOTION.put(profile, motion);
			}
			motion.update(state, engine.getTicks(), engine.getTime(), bindings);

			byte[] data = prepareData(state, def, bindings);
			byte[] prev = state.prevGestureData != null && state.prevGestureData.length == data.length ? state.prevGestureData : null;
			for (int i = 0; i < bindings.size(); i++) {
				LayerBinding b = bindings.get(i);
				int p = b.parameter;
				if (p < 0 || p >= data.length) continue;
				byte v;
				if (b.isToggle()) {
					boolean on = motion.isOn(b);
					v = (byte) (on ? (data[p] | b.mask) : (data[p] & ~b.mask));
				} else {
					v = (byte) Math.round(motion.value(b) * 255f);
				}
				data[p] = v;
				if (prev != null) prev[p] = v;
			}

			if (!announced) {
				announced = true;
				Log.info("[Compelium's CPM+] Physics triggers are running");
			}
		} catch (Throwable e) {
			errors++;
			Log.warn("[Compelium's CPM+] Error while applying triggers" + (errors >= MAX_ERRORS ? ", turning triggers off until the game restarts" : ""), e);
		}
	}

	/**
	 * Returns the animation value array for this frame, creating it from the model's
	 * defaults when CPM has none (for example on servers without CPM), or growing it
	 * when it is shorter than the model needs.
	 */
	private static byte[] prepareData(AnimationState state, ModelDefinition def, List<LayerBinding> bindings) {
		int needed = 0;
		for (int i = 0; i < bindings.size(); i++) {
			needed = Math.max(needed, bindings.get(i).parameter + 1);
		}
		byte[] data = state.gestureData;
		if (data == null) {
			ParameterDetails params = def.getAnimations().getParams();
			data = params.createSyncParams();
		}
		if (data.length < needed) {
			ParameterDetails params = def.getAnimations().getParams();
			byte[] grown = params.createSyncParams();
			if (grown.length < needed) {
				byte[] g2 = new byte[needed];
				System.arraycopy(grown, 0, g2, 0, grown.length);
				grown = g2;
			}
			System.arraycopy(data, 0, grown, 0, data.length);
			data = grown;
		}
		state.gestureData = data;
		return data;
	}
}
