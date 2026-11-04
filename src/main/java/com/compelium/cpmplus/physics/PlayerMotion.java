package com.compelium.cpmplus.physics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.animation.AnimationState;

/**
 * Movement history and spring state for one player, as seen on this computer.
 *
 * Everything here is worked out from what CPM already fills in for every player
 * it draws (position change per tick, body rotation, swimming), so it works the same
 * for you and for everyone around you, without any help from the server.
 */
final class PlayerMotion {
	/** Reference speeds, in blocks per tick, that reach the end of the animation at forca=5. */
	static final float REF_FALL = 0.8f;
	static final float REF_RISE = 0.42f;
	static final float REF_FORWARD = 0.28f;
	static final float REF_SIDE = 0.22f;
	static final float REF_SPEED = 0.28f;
	/** Degrees per tick. */
	static final float REF_TURN = 15f;
	/** Seconds standing still to reach the end of the animation at forca=5. */
	static final float REF_STILL = 5f;

	private static final float E = (float) Math.E;
	private static final float TELEPORT = 4f;

	// Movement measured on the last tick
	float fall, rise, forward, side, speed, turn;
	boolean inWater;
	float stillSeconds;

	// One-off events (landing and jumping) are counted, and every spring reacts once per new event
	int impactEvent, jumpEvent;
	float impactStrength;

	private float fallPeak;
	private int groundTicks;
	private boolean jumpPending;
	private float jumpPendingDy;
	private float lastBodyYaw;
	private boolean hasYaw;
	private int lastTick = Integer.MIN_VALUE;
	private long lastTime = Long.MIN_VALUE;

	private final Map<String, Spring> springs = new HashMap<>();

	void update(AnimationState state, int tick, long timeMs, List<LayerBinding> bindings) {
		boolean reset = false;
		if (tick != lastTick) {
			int elapsed = lastTick == Integer.MIN_VALUE ? 1 : tick - lastTick;
			if (elapsed <= 0 || elapsed > 40) {
				clearHistory();
				elapsed = 1;
				reset = true;
			}
			onTick(state, elapsed);
			lastTick = tick;
		}

		float dt = lastTime == Long.MIN_VALUE ? 0 : (timeMs - lastTime) / 1000f;
		if (dt < 0 || dt > 0.5f) {
			dt = 0;
			reset = true;
		}
		lastTime = timeMs;

		for (int i = 0; i < bindings.size(); i++) {
			LayerBinding b = bindings.get(i);
			Spring sp = springs.get(b.key);
			if (sp == null) {
				sp = new Spring();
				springs.put(b.key, sp);
			}
			TriggerSettings st = b.settings;
			float target = target(st);
			if (reset || !sp.initialized) {
				sp.reset(target);
				sp.lastImpact = impactEvent;
				sp.lastJump = jumpEvent;
				continue;
			}
			if (st.type == TriggerType.POUSO && sp.lastImpact != impactEvent) {
				sp.lastImpact = impactEvent;
				sp.velocity += Math.min(st.limit, impactStrength * st.gain) * st.omega * E;
			}
			if (st.type == TriggerType.PULO && sp.lastJump != jumpEvent) {
				sp.lastJump = jumpEvent;
				sp.velocity += Math.min(st.limit, st.gain) * st.omega * E;
			}
			sp.step(target, st.omega, st.zeta, dt);
		}
	}

	/** Current animation position for a layer, 0 to 1. */
	float value(LayerBinding b) {
		Spring sp = springs.get(b.key);
		if (sp == null) return b.settings.type.twoWay ? 0.5f : 0f;
		TriggerSettings st = b.settings;
		if (st.type.twoWay) {
			return TriggerSettings.clamp(sp.position, 0.5f - 0.5f * st.limit, 0.5f + 0.5f * st.limit);
		}
		return TriggerSettings.clamp(sp.position, 0f, st.limit);
	}

	/** On/off state for an on/off layer, with a small margin so it doesn't flicker. */
	boolean isOn(LayerBinding b) {
		Spring sp = springs.get(b.key);
		if (sp == null) return false;
		float v = value(b);
		float strength = b.settings.type.twoWay ? Math.abs(v - 0.5f) * 2f : v;
		float on = b.settings.threshold;
		float off = on * 0.8f;
		if (sp.toggleOn) {
			if (strength < off) sp.toggleOn = false;
		} else if (strength >= on) {
			sp.toggleOn = true;
		}
		return sp.toggleOn;
	}

	private float target(TriggerSettings st) {
		float g = st.gain;
		float lim = st.limit;
		switch (st.type) {
		case QUEDA: return clamp01(fall / REF_FALL * g, lim);
		case SUBIDA: return clamp01(rise / REF_RISE * g, lim);
		case FRENTE: return clamp01(Math.max(0, forward) / REF_FORWARD * g, lim);
		case TRAS: return clamp01(Math.max(0, -forward) / REF_FORWARD * g, lim);
		case ESQUERDA: return clamp01(Math.max(0, -side) / REF_SIDE * g, lim);
		case DIREITA: return clamp01(Math.max(0, side) / REF_SIDE * g, lim);
		case VELOCIDADE: return clamp01(speed / REF_SPEED * g, lim);
		case GIRO_ESQUERDA: return clamp01(Math.max(0, -turn) / REF_TURN * g, lim);
		case GIRO_DIREITA: return clamp01(Math.max(0, turn) / REF_TURN * g, lim);
		case POUSO:
		case PULO: return 0f;
		case AGUA: return clamp01(inWater ? 1f : 0f, lim);
		case PARADO: return clamp01(stillSeconds / (REF_STILL / g), lim);

		case VERTICAL: return twoWay(fall > 0 ? fall / REF_FALL : -rise / REF_RISE, g, lim);
		case FRENTE_TRAS: return twoWay(forward / REF_FORWARD, g, lim);
		case LADOS: return twoWay(side / REF_SIDE, g, lim);
		case GIRO: return twoWay(turn / REF_TURN, g, lim);
		default: return 0f;
		}
	}

	private void onTick(AnimationState s, int elapsed) {
		float dx = s.moveAmountX, dy = s.moveAmountY, dz = s.moveAmountZ;
		if (!finite(dx) || !finite(dy) || !finite(dz) || Math.abs(dx) > TELEPORT || Math.abs(dy) > TELEPORT || Math.abs(dz) > TELEPORT) {
			dx = dy = dz = 0;
		}

		double yawRad = Math.toRadians(s.bodyYaw);
		float sin = (float) Math.sin(yawRad);
		float cos = (float) Math.cos(yawRad);
		// Minecraft faces (-sin, cos) at a given yaw; the player's right hand side is (-cos, -sin)
		forward = -dx * sin + dz * cos;
		side = -dx * cos - dz * sin;
		speed = (float) Math.sqrt(dx * dx + dz * dz);
		fall = Math.max(0, -dy);
		rise = Math.max(0, dy);
		inWater = s.retroSwimming || s.swimming;

		if (hasYaw && finite(s.bodyYaw)) {
			turn = wrapDegrees(s.bodyYaw - lastBodyYaw) / elapsed;
			if (Math.abs(turn) > 90f) turn = 0;
		} else {
			turn = 0;
		}
		if (finite(s.bodyYaw)) {
			lastBodyYaw = s.bodyYaw;
			hasYaw = true;
		}

		boolean flying = s.elytraFlying || s.syncState != null && s.syncState.creativeFlying;
		boolean airborneEffectsOff = flying || s.dying || s.sleeping;

		// Landing: remember the fastest fall, then fire once the fall stops
		if (airborneEffectsOff) {
			fallPeak = 0;
		} else if (dy < -0.05f) {
			fallPeak = Math.max(fallPeak, -dy);
		} else {
			if (fallPeak >= 0.2f) {
				impactStrength = Math.min(1f, fallPeak / REF_FALL);
				impactEvent++;
			}
			fallPeak = 0;
		}

		// Jumping: a rise from solid ground that keeps going on the next tick
		// (a step up a block also rises, but stops right away)
		if (jumpPending) {
			jumpPending = false;
			if (dy >= 0.12f && dy >= jumpPendingDy * 0.72f) jumpEvent++;
		} else if (groundTicks >= 2 && dy >= 0.1f && dy <= 0.8f && !inWater && !s.isOnLadder && !airborneEffectsOff) {
			jumpPending = true;
			jumpPendingDy = dy;
		}
		groundTicks = Math.abs(dy) < 0.02f ? groundTicks + elapsed : 0;

		if (speed < 0.005f && Math.abs(dy) < 0.005f && Math.abs(turn) < 1f) {
			stillSeconds = Math.min(3600f, stillSeconds + 0.05f * elapsed);
		} else {
			stillSeconds = 0;
		}
	}

	private void clearHistory() {
		fallPeak = 0;
		groundTicks = 0;
		jumpPending = false;
		hasYaw = false;
		stillSeconds = 0;
	}

	private static float twoWay(float signed, float gain, float limit) {
		float s = TriggerSettings.clamp(signed * gain, -limit, limit);
		return 0.5f + 0.5f * s;
	}

	private static float clamp01(float v, float limit) {
		return TriggerSettings.clamp(v, 0f, limit);
	}

	private static boolean finite(float f) {
		return !Float.isNaN(f) && !Float.isInfinite(f);
	}

	static float wrapDegrees(float deg) {
		float d = deg % 360f;
		if (d >= 180f) d -= 360f;
		if (d < -180f) d += 360f;
		return d;
	}
}
