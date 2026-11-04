package com.compelium.cpmplus.physics;

/**
 * Links one of this mod's animations in a model to the CPM value it drives.
 */
final class LayerBinding {
	/** The animation name as written in the editor (the part before any '#'). */
	final String key;
	final TriggerSettings settings;
	/** Index of the CPM parameter byte this animation reads. */
	final int parameter;
	/** Bit mask for on/off layers; 0 for value (slider) layers. */
	final int mask;

	LayerBinding(String key, TriggerSettings settings, int parameter, int mask) {
		this.key = key;
		this.settings = settings;
		this.parameter = parameter;
		this.mask = mask;
	}

	boolean isToggle() {
		return mask != 0;
	}
}
