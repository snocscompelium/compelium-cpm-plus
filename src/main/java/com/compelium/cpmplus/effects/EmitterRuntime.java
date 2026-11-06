package com.compelium.cpmplus.effects;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import com.compelium.cpmplus.physics.PlayerMotion;
import com.compelium.cpmplus.physics.TriggerType;
import com.tom.cpm.shared.model.render.ItemRenderer;

/**
 * Turns marker positions into particles: works out the world position and direction,
 * keeps a steady rate whatever the frame rate, and draws a smooth trail when moving.
 */
final class EmitterRuntime {
	/** Most particles one emitter may add in a single frame. */
	static final int MAX_PER_EMITTER_FRAME = 60;
	/** Most particles all emitters together may add in a single frame. */
	static final int FRAME_BUDGET = 400;
	/** Farther than this between frames counts as a teleport: no trail is drawn. */
	static final double MAX_TRAIL = 4.0;

	private static final Map<Object, IdentityHashMap<ItemRenderer, EmitterState[]>> STATES = new WeakHashMap<>();
	private static final Object NO_OWNER = new Object();
	private static final Random RNG = new Random();
	private static long budgetStamp;
	private static int budget = FRAME_BUDGET;

	private EmitterRuntime() {
	}

	static void emit(RenderCapture.Frame f, double camX, double camY, double camZ, ParticleSink sink, long now) {
		if (now - budgetStamp > 8_000_000L) {
			budgetStamp = now;
			budget = FRAME_BUDGET;
		}
		Object owner = f.owner != null ? f.owner : NO_OWNER;
		IdentityHashMap<ItemRenderer, EmitterState[]> states = STATES.get(owner);
		if (states == null) {
			states = new IdentityHashMap<>();
			STATES.put(owner, states);
		}
		double[] root = Mat.fromColumnMajor(f.root);
		for (RenderCapture.Captured c : f.items) {
			EmitterState[] st = states.get(c.renderer);
			if (st == null || st.length != c.emitters.length) {
				st = new EmitterState[c.emitters.length];
				for (int i = 0; i < st.length; i++) st[i] = new EmitterState();
				states.put(c.renderer, st);
			}
			double[] world = Mat.mul(root, Mat.fromRowMajor(c.local));
			double px = world[12] + camX, py = world[13] + camY, pz = world[14] + camZ;
			double[] dir = Mat.normalize(-world[4], -world[5], -world[6]);
			for (int i = 0; i < c.emitters.length; i++) {
				emitOne(c.emitters[i], st[i], c.visible, f.motion, px, py, pz, dir, sink, now);
			}
		}
	}

	static void emitOne(EmitterSettings es, EmitterState st, boolean visible, PlayerMotion motion,
			double px, double py, double pz, double[] dir, ParticleSink sink, long now) {
		float dt = st.lastNanos == 0 ? 0f : (now - st.lastNanos) / 1e9f;
		boolean gap = st.lastNanos == 0 || dt > 0.25f;
		st.lastNanos = now;
		if (dt < 0) dt = 0;
		if (dt > 0.1f) dt = 0.1f;

		int burst = 0;
		float factor = 1f;
		if (es.gatilho != null) {
			factor = 0f;
			if (motion != null) {
				if (es.gatilho == TriggerType.POUSO) {
					if (st.seen && st.lastEvent != motion.impactEvent()) burst = Math.round(es.qtd * motion.impactStrength());
					st.lastEvent = motion.impactEvent();
				} else if (es.gatilho == TriggerType.PULO) {
					if (st.seen && st.lastEvent != motion.jumpEvent()) burst = Math.round(es.qtd);
					st.lastEvent = motion.jumpEvent();
				} else {
					factor = motion.gate(es.gatilho);
				}
			}
		}
		st.seen = true;

		if (!visible) {
			st.acc = 0;
			st.hasPos = false;
			return;
		}

		st.acc += es.qtd * factor * dt;
		int n = (int) st.acc;
		st.acc -= n;
		n += burst;
		if (n > MAX_PER_EMITTER_FRAME) n = MAX_PER_EMITTER_FRAME;
		if (n > budget) n = budget;
		budget -= n;

		boolean trail = st.hasPos && !gap && dist(st.x, st.y, st.z, px, py, pz) < MAX_TRAIL;
		for (int k = 0; k < n; k++) {
			double t = trail ? (k + 1.0) / n : 1.0;
			double x = st.x + (px - st.x) * t, y = st.y + (py - st.y) * t, z = st.z + (pz - st.z) * t;
			if (!trail) {
				x = px;
				y = py;
				z = pz;
			}
			double[] d = randomCone(dir, es.spreadRadians, RNG);
			sink.spawn(es.particleCommand, x, y, z, d[0] * es.speed, d[1] * es.speed, d[2] * es.speed);
		}
		st.x = px;
		st.y = py;
		st.z = pz;
		st.hasPos = true;
	}

	/** A random direction at most {@code angle} radians away from {@code dir}. */
	static double[] randomCone(double[] dir, double angle, Random rng) {
		if (angle <= 1e-4) return dir;
		double cosMax = Math.cos(Math.min(angle, Math.PI));
		double cosT = 1 - rng.nextDouble() * (1 - cosMax);
		double sinT = Math.sqrt(Math.max(0, 1 - cosT * cosT));
		double phi = rng.nextDouble() * Math.PI * 2;
		double[] a = Math.abs(dir[0]) < 0.9 ? new double[] {1, 0, 0} : new double[] {0, 1, 0};
		double[] e1 = Mat.normalize(Mat.cross(dir, a));
		double[] e2 = Mat.cross(dir, e1);
		double c = Math.cos(phi) * sinT, s = Math.sin(phi) * sinT;
		return new double[] {
				dir[0] * cosT + e1[0] * c + e2[0] * s,
				dir[1] * cosT + e1[1] * c + e2[1] * s,
				dir[2] * cosT + e1[2] * c + e2[2] * s
		};
	}

	private static double dist(double ax, double ay, double az, double bx, double by, double bz) {
		double dx = ax - bx, dy = ay - by, dz = az - bz;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	static final class EmitterState {
		float acc;
		long lastNanos;
		double x, y, z;
		boolean hasPos, seen;
		int lastEvent;
	}
}
