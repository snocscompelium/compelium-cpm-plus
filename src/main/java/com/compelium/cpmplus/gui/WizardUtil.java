package com.compelium.cpmplus.gui;

import com.compelium.cpmplus.physics.TriggerSettings;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.EditorAnim;

/**
 * Helpers shared by the editor assistants.
 */
final class WizardUtil {
	private WizardUtil() {
	}

	/**
	 * The interpolation this mod uses for movement animations: Trigonometric Single.
	 *
	 * CPM's "single" interpolations (and Snap) spread the frames evenly, so with three frames
	 * 50% lands exactly on the middle one. The looping ones wrap around to the first frame and
	 * Polynomial Single bends the curve, so 50% lands between frames. Trigonometric also eases
	 * in and out of every frame, so small movements around the rest pose barely move the part.
	 * Snap is kept because it is already aligned and someone may want the stepped look.
	 */
	static final InterpolatorType DEFAULT = InterpolatorType.TRIG_SINGLE;

	static InterpolatorType aligned(InterpolatorType t) {
		if (t == InterpolatorType.TRIG_SINGLE || t == InterpolatorType.NO_INTERPOLATE) return t;
		return DEFAULT;
	}

	/** The part of an animation name before '#'. */
	static String baseName(String displayName) {
		if (displayName == null) return "";
		int hash = displayName.indexOf('#');
		return hash >= 0 ? displayName.substring(0, hash) : displayName;
	}

	/** The '#...' part of an animation name, or an empty string. */
	static String suffix(String displayName) {
		if (displayName == null) return "";
		int hash = displayName.indexOf('#');
		return hash >= 0 ? displayName.substring(hash) : "";
	}

	/**
	 * Switches every movement animation of this mod to an interpolation that keeps the
	 * frames in the right place. Returns how many animations changed.
	 */
	static int fixAlignment(Editor editor) {
		ActionBuilder ab = editor.action("edit", "action.cpm.anim");
		int changed = 0;
		for (EditorAnim anim : editor.animations) {
			if (anim.type != AnimationType.VALUE_LAYER && anim.type != AnimationType.LAYER) continue;
			if (TriggerSettings.parse(baseName(anim.displayName)) == null) continue;
			InterpolatorType fixed = aligned(anim.intType);
			if (fixed == anim.intType) continue;
			ab.updateValueOp(anim, anim.intType, fixed, (a, b) -> a.intType = b);
			ab.onAction(anim, EditorAnim::clearCache);
			changed++;
		}
		if (changed > 0) {
			ab.execute();
			editor.updateGui();
		}
		return changed;
	}
}
