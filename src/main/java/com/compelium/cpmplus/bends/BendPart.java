package com.compelium.cpmplus.bends;

import com.tom.cpm.shared.model.PlayerModelParts;

/**
 * The body parts an emote can bend.
 *
 * Arms and legs bend at the middle of the limb (elbow / knee): the lower half turns.
 * The torso bends at the waist: the upper half (chest) turns.
 */
public enum BendPart {
	LEFT_ARM("left_arm", "leftArm", PlayerModelParts.LEFT_ARM),
	RIGHT_ARM("right_arm", "rightArm", PlayerModelParts.RIGHT_ARM),
	LEFT_LEG("left_leg", "leftLeg", PlayerModelParts.LEFT_LEG),
	RIGHT_LEG("right_leg", "rightLeg", PlayerModelParts.RIGHT_LEG),
	TORSO("torso", "torso", PlayerModelParts.BODY);

	public static final BendPart[] VALUES = values();

	/** Bone name used by Player Animation Library (Emotecraft 3.x). */
	public final String palBone;
	/** Part key used by playerAnimator (Emotecraft 2.x). */
	public final String playerAnimatorKey;
	/** The vanilla part (CPM root) this bend belongs to. */
	public final PlayerModelParts cpmPart;

	BendPart(String palBone, String playerAnimatorKey, PlayerModelParts cpmPart) {
		this.palBone = palBone;
		this.playerAnimatorKey = playerAnimatorKey;
		this.cpmPart = cpmPart;
	}

	public boolean isTorso() {
		return this == TORSO;
	}

	public static BendPart byPalBone(String bone) {
		if (bone == null) return null;
		switch (bone) {
		case "left_arm": return LEFT_ARM;
		case "right_arm": return RIGHT_ARM;
		case "left_leg": return LEFT_LEG;
		case "right_leg": return RIGHT_LEG;
		case "torso": return TORSO;
		default: return null;
		}
	}
}
