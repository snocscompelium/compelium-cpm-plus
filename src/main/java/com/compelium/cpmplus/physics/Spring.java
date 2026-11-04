package com.compelium.cpmplus.physics;

/**
 * A damped spring that follows a target value. This is what makes a part lag
 * behind and wobble instead of snapping straight to the new position.
 */
final class Spring {
	private static final float MAX_STEP = 1f / 120f;

	float position, velocity;
	boolean initialized;
	/** Remembered on/off state, used for on/off layers so they don't flicker at the edge. */
	boolean toggleOn;
	/** Last landing and jump events this spring already reacted to. */
	int lastImpact, lastJump;

	void reset(float target) {
		position = target;
		velocity = 0;
		initialized = true;
	}

	void step(float target, float omega, float zeta, float dt) {
		if (!initialized) {
			reset(target);
			return;
		}
		if (dt <= 0) return;
		int steps = Math.max(1, (int) Math.ceil(dt / MAX_STEP));
		float h = dt / steps;
		float k = omega * omega;
		float c = 2f * zeta * omega;
		for (int i = 0; i < steps; i++) {
			float accel = k * (target - position) - c * velocity;
			velocity += accel * h;
			position += velocity * h;
		}
		if (Float.isNaN(position) || Float.isInfinite(position) || Float.isNaN(velocity) || Float.isInfinite(velocity)) {
			reset(target);
		}
	}
}
