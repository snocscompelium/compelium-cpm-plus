package com.compelium.cpmplus.bends;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;
import com.tom.cpm.shared.parts.anim.AnimatorChannel;
import com.tom.cpm.shared.parts.anim.Float3Driver;
import com.tom.cpm.shared.parts.anim.Float3Driver.Float3Consumer;
import com.tom.cpm.shared.parts.anim.SerializedAnimation;
import com.tom.cpm.shared.parts.anim.SerializedTrigger;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.CustomPoseGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.DropdownButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;

/**
 * CPM does not export the names of model pieces, only the names of animations.
 * So the modeler marks the pieces with an animation named like {@code bend_left_arm}:
 * every piece used in that animation becomes a bending piece.
 * The marker animation itself is never played in game.
 *
 * <p>This class reads CPM's newer animation format ({@code ModelPartAnimationNew}, written when
 * "experimental export" is on). The default format is read by {@link LegacyBendReader}.
 */
public final class BendRigBuilder {
	private BendRigBuilder() {}

	public static void build(AnimLoaderState state, ModelDefinition def) {
		List<AbstractGestureButtonData> buttons = state.getGestureButtons();
		Map<Integer, SerializedTrigger> triggers = state.getTriggers();
		Map<Integer, SerializedAnimation> anims = state.getAnims();
		if (buttons == null || triggers == null || anims == null) return;

		Map<Integer, BendName> markerTriggers = new HashMap<>();
		for (AbstractGestureButtonData b : buttons) {
			if (b instanceof DropdownButtonData) {
				DropdownButtonData dd = (DropdownButtonData) b;
				for (int i = 0; i < dd.opts.size(); i++) {
					BendName bn = BendName.parse(dd.opts.get(i));
					if (bn == null) continue;
					for (Map.Entry<Integer, SerializedTrigger> e : triggers.entrySet()) {
						SerializedTrigger t = e.getValue();
						if (t.anim == AnimationType.LAYER && !t.bitMask && t.parameter == dd.parameter && t.value == i) {
							markerTriggers.put(e.getKey(), bn);
						}
					}
				}
				continue;
			}
			BendName bn = BendName.parse(b.getName());
			if (bn == null) continue;
			for (Map.Entry<Integer, SerializedTrigger> e : triggers.entrySet()) {
				if (matches(b, e.getValue())) markerTriggers.put(e.getKey(), bn);
			}
		}
		if (markerTriggers.isEmpty()) return;

		RigCollector rc = new RigCollector();
		for (BendName bn : markerTriggers.values()) rc.marker(bn);
		for (SerializedAnimation a : anims.values()) {
			BendName bn = markerTriggers.get(a.triggerID);
			if (bn == null || a.animatorChannels == null) continue;
			for (AnimatorChannel ch : a.animatorChannels.values()) {
				if (ch == null || ch.part == null) continue;
				Float3Consumer f3 = Float3Driver.getFromPart(ch.part);
				if (f3 instanceof AnimatorChannel.CubeFloatDriver) {
					rc.add(bn, ((AnimatorChannel.CubeFloatDriver) f3).cubeId);
				}
			}
		}

		// Never play the marker animations themselves.
		for (Integer id : markerTriggers.keySet()) {
			SerializedTrigger t = triggers.get(id);
			if (t instanceof TriggerMarker) ((TriggerMarker) t).cpmplus$setMarker(true);
		}

		rc.finish(def);
	}

	private static boolean matches(AbstractGestureButtonData b, SerializedTrigger t) {
		if (t == null || t.anim == null) return false;
		if (b instanceof ValueParameterButtonData) {
			return t.anim == AnimationType.VALUE_LAYER && t.parameter == ((ValueParameterButtonData) b).parameter;
		}
		if (b instanceof BoolParameterToggleButtonData) {
			BoolParameterToggleButtonData l = (BoolParameterToggleButtonData) b;
			return t.anim == AnimationType.LAYER && t.parameter == l.parameter && t.value == l.mask;
		}
		if (b instanceof CustomPoseGestureButtonData) {
			CustomPoseGestureButtonData c = (CustomPoseGestureButtonData) b;
			AnimationType type = c.isPose() ? AnimationType.CUSTOM_POSE : AnimationType.GESTURE;
			return t.anim == type && t.value == c.id;
		}
		return false;
	}
}
