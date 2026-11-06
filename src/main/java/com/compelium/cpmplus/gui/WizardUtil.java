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
	 * CPM's value layers spread the frames evenly only with the "single" linear and
	 * trigonometric interpolations (and with no interpolation). The others place the last
	 * frame before the end, so 50% lands between frames instead of on the middle one.
	 */
	static InterpolatorType aligned(InterpolatorType t) {
		if (t == null) return InterpolatorType.LINEAR_SINGLE;
		switch (t) {
		case LINEAR_SINGLE:
		case TRIG_SINGLE:
		case NO_INTERPOLATE:
			return t;
		case TRIG_LOOP:
			return InterpolatorType.TRIG_SINGLE;
		default:
			return InterpolatorType.LINEAR_SINGLE;
		}
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
