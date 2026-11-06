package com.compelium.cpmplus.bends;

import java.lang.reflect.Field;

import com.tom.cpm.shared.animation.AnimationEngine.AnimationMode;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ModelRenderManager.RedirectHolder;

/**
 * Runs inside CPM's {@code RedirectHolder.bindFirstSetup}, after CPM applied the model's own
 * animations and the vanilla pose, and right before CPM's "copy transform" step.
 */
public final class BendApplier {
	private BendApplier() {}

	private static boolean disabled;
	private static int errors;

	// The main player model computes the bends; armor/elytra/cape sub-models of the same
	// player reuse them (they are drawn right after, with the same model definition).
	private static ModelDefinition lastDef;
	private static BendFrame lastFrame;

	private static boolean firstBendLogged;

	public static void onPoseReady(Object holderObj) {
		if (disabled || !Bends.enabled()) return;
		try {
			RedirectHolder<?, ?, ?, ?> holder = (RedirectHolder<?, ?, ?, ?>) holderObj;
			ModelDefinition def = holder.def;
			if (!(def instanceof RigHolder)) return;
			RigHolder rh = (RigHolder) def;
			BendRig rig = rh.cpmplus$getRig();
			int flags = rh.cpmplus$getFlags();
			if ((flags & RigHolder.FLAG_REPORTED) == 0) {
				rh.cpmplus$setFlags(flags | RigHolder.FLAG_REPORTED);
				report(def, rig, flags);
			}
			if (rig == null) return;
			if (!HolderAccess.isPlayerMode(holder)) return;
			BendFrame frame;
			boolean main = HolderAccess.isMainModel(holder);
			if (main) {
				frame = BendSources.collect(holder.model);
				lastDef = def;
				lastFrame = frame;
			} else {
				frame = def == lastDef ? lastFrame : null;
			}
			if (frame != null) {
				rig.apply(def, frame, main);
				if (main && !firstBendLogged) logFirstBend(rig, frame);
			}
		} catch (Throwable t) {
			if (++errors >= 5) disabled = true;
			Bends.warn(disabled ? "Repeated errors; emote bends are off until the game restarts." : "Error while applying an emote bend.", t);
		}
	}

	/** Once per loaded model of the local player: says in the log whether this model will bend. */
	private static void report(ModelDefinition def, BendRig rig, int flags) {
		if (!RigCollector.isLocalPlayer(def)) return;
		if (rig != null) {
			Bends.info("CPM model connected: the marked pieces will bend in emotes.");
		} else if ((flags & RigHolder.FLAG_MARKERS_SEEN) == 0) {
			Bends.debug("This CPM model has no bend animations (like bend_left_arm), so nothing in it bends.");
		}
	}

	/** Once per game session: the first time a marked piece actually turns. */
	private static void logFirstBend(BendRig rig, BendFrame frame) {
		for (BendRig.Group g : rig.groups()) {
			double angle = frame.bend[g.part.ordinal()] * g.factor;
			if (Math.abs(angle) < 0.01) continue;
			firstBendLogged = true;
			Bends.info("First emote bend applied: " + g.label + " (" + Math.round(Math.toDegrees(angle)) + " degrees).");
			return;
		}
	}

	/** Reads RedirectHolder fields that changed between CPM versions. */
	static final class HolderAccess {
		private static boolean init;
		private static Field parent, animState, mode;

		private HolderAccess() {}

		private static void init() {
			init = true;
			parent = field("parent");
			animState = field("animState"); // CPM 0.6.26+
			mode = field("mode");           // CPM 0.6.20 - 0.6.25
		}

		private static Field field(String name) {
			try {
				Field f = RedirectHolder.class.getDeclaredField(name);
				f.setAccessible(true);
				return f;
			} catch (Throwable t) {
				return null;
			}
		}

		/** Sub-models (armor, elytra, cape...) have a parent holder; the player model does not. */
		static boolean isMainModel(Object holder) throws IllegalAccessException {
			if (!init) init();
			return parent == null || parent.get(holder) == null;
		}

		/** Only bend the full third-person/inventory model, not first-person hands or skulls. */
		static boolean isPlayerMode(Object holder) throws IllegalAccessException {
			if (!init) init();
			Object m = null;
			if (animState != null) {
				Object st = animState.get(holder);
				if (st instanceof AnimationState) m = ((AnimationState) st).animationMode;
			} else if (mode != null) {
				m = mode.get(holder);
			}
			return m == null || m == AnimationMode.PLAYER;
		}
	}
}
