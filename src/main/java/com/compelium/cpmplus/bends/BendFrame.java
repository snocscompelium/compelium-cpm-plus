package com.compelium.cpmplus.bends;

/**
 * The bends of one player for the frame being rendered, in radians.
 *
 * For each part: {@code bend} is the bend angle and {@code axis} turns the bend plane around
 * the limb (playerAnimator only; Player Animation Library always bends around the X axis).
 */
public final class BendFrame {
	public final float[] axis = new float[BendPart.VALUES.length];
	public final float[] bend = new float[BendPart.VALUES.length];

	/**
	 * playerAnimator only: legacy "body" bend that the head and arms must follow.
	 * Player Animation Library already moves the head and arms itself.
	 */
	public float upperAxis, upperBend;

	public boolean any() {
		for (float b : bend) if (b != 0) return true;
		return upperBend != 0;
	}

	public void set(BendPart part, float axisAngle, float bendAngle) {
		axis[part.ordinal()] = axisAngle;
		bend[part.ordinal()] = bendAngle;
	}
}
