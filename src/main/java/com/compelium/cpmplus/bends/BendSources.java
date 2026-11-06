package com.compelium.cpmplus.bends;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Reads the current emote bends.
 *
 * <ul>
 * <li><b>Player Animation Library</b> (Emotecraft 3.x, Minecraft 1.21.7+): every time the
 * library computes a body bone for the player being drawn, the bend is copied here (see
 * {@code AnimationStackMixin}). Values only count if they were computed after CPM started
 * drawing the current player, so a player without CPM can never lend his bend to another.</li>
 * <li><b>playerAnimator</b> (Emotecraft 2.x, Minecraft 1.21.4 - 1.21.5): read straight from the
 * player model, where playerAnimator stores the animation of the player being drawn.</li>
 * </ul>
 * Everything here runs on the render thread.
 */
public final class BendSources {
	private BendSources() {}

	private static volatile Thread renderThread;
	private static int drawSeq;

	// ---- Player Animation Library ----
	private static final float[] palBend = new float[BendPart.VALUES.length];
	private static int palSeq = -1;
	private static boolean palBroken, palSeen;

	/** Called whenever CPM starts drawing a model (RedirectHolder.swapIn). */
	public static void onDrawStart() {
		Thread t = Thread.currentThread();
		if (renderThread != t) renderThread = t;
		drawSeq++;
	}

	/** Called by the PAL mixin after a bone transform was computed (PAL types stay inside the mixin). */
	public static void onPalBone(String boneName, float bend) {
		if (Thread.currentThread() != renderThread) return;
		BendPart part = BendPart.byPalBone(boneName);
		if (part == null) return;
		if (!palSeen) {
			palSeen = true;
			Bends.info("Player Animation Library found (Emotecraft 3.x): emote bends are on.");
		}
		if (palSeq != drawSeq) {
			Arrays.fill(palBend, 0f);
			palSeq = drawSeq;
		}
		palBend[part.ordinal()] = bend;
	}

	/** Called once if PAL's bone class is not what this mod expects. */
	public static void onPalBroken(Throwable t) {
		if (palBroken) return;
		palBroken = true;
		Bends.warn("Could not read the bends from Player Animation Library; that part is off.", t);
	}

	public static boolean isPalBroken() {
		return palBroken;
	}

	/** @return the bends of the player CPM is drawing now, or null if nothing bends. */
	public static BendFrame collect(Object playerModel) {
		BendFrame f = new BendFrame();
		if (palSeq == drawSeq) {
			for (BendPart p : BendPart.VALUES) {
				float b = palBend[p.ordinal()];
				if (b != 0) f.set(p, 0, b);
			}
		}
		PlayerAnimatorAccess.read(playerModel, f);
		return f.any() ? f : null;
	}

	/** Reflection access to playerAnimator (dev.kosmx.playerAnim), so no compile-time dependency is needed. */
	static final class PlayerAnimatorAccess {
		private static boolean init, available;
		private static Class<?> mutableModel;
		private static Method getAnimation, isActive, getBend, left, right;
		private static Object[] keys;
		private static Object keyBody;

		private PlayerAnimatorAccess() {}

		private static void init() {
			init = true;
			try {
				ClassLoader cl = BendSources.class.getClassLoader();
				mutableModel = Class.forName("dev.kosmx.playerAnim.impl.IMutableModel", false, cl);
				Class<?> processor = Class.forName("dev.kosmx.playerAnim.core.impl.AnimationProcessor", false, cl);
				Class<?> partKey = Class.forName("dev.kosmx.playerAnim.api.PartKey", false, cl);
				Class<?> pair = Class.forName("dev.kosmx.playerAnim.core.util.Pair", false, cl);
				getAnimation = mutableModel.getMethod("playerAnimator$getAnimation");
				isActive = processor.getMethod("isActive");
				getBend = processor.getMethod("getBend", partKey);
				left = pair.getMethod("getLeft");
				right = pair.getMethod("getRight");
				Method keyForId = partKey.getMethod("keyForId", String.class);
				keys = new Object[BendPart.VALUES.length];
				for (BendPart p : BendPart.VALUES) keys[p.ordinal()] = keyForId.invoke(null, p.playerAnimatorKey);
				keyBody = keyForId.invoke(null, "body");
				available = true;
				Bends.info("playerAnimator found (Emotecraft 2.x): emote bends are on.");
			} catch (ClassNotFoundException e) {
				available = false;
			} catch (Throwable t) {
				available = false;
				Bends.warn("playerAnimator found, but in a version this mod does not know; its bends are off.", t);
			}
		}

		static void read(Object model, BendFrame f) {
			if (!init) init();
			if (!available || model == null || !mutableModel.isInstance(model)) return;
			try {
				Object proc = getAnimation.invoke(model);
				if (proc == null || !Boolean.TRUE.equals(isActive.invoke(proc))) return;
				for (BendPart p : BendPart.VALUES) {
					float[] ab = pair(getBend.invoke(proc, keys[p.ordinal()]));
					if (p.isTorso()) {
						float[] body = pair(getBend.invoke(proc, keyBody));
						// Same as playerAnimator itself: the torso bends by torso + body,
						// and the head and arms follow only the legacy "body" bend.
						f.upperAxis = body[0];
						f.upperBend = body[1];
						ab[0] += body[0];
						ab[1] += body[1];
					}
					if (ab[1] != 0) f.set(p, ab[0], ab[1]);
				}
			} catch (Throwable t) {
				available = false;
				Bends.warn("Error while reading the playerAnimator bends; that part is off.", t);
			}
		}

		private static float[] pair(Object pair) throws ReflectiveOperationException {
			if (pair == null) return new float[2];
			Object a = left.invoke(pair), b = right.invoke(pair);
			return new float[] {a instanceof Number ? ((Number) a).floatValue() : 0, b instanceof Number ? ((Number) b).floatValue() : 0};
		}
	}
}
