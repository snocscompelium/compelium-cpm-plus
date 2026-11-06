package com.compelium.cpmplus.physics;

/**
 * Links one of this mod's movement animations in a model to the CPM value it drives.
 */
public final class LayerBinding {
	/** The animation name as written in the editor (the part before any '#'). */
	public final String key;
	public final TriggerSettings settings;
	/** Index of the CPM parameter byte this animation reads. */
	public final int parameter;
	/** Bit mask for on/off layers; 0 for value (slider) layers. */
	public final int mask;

	public LayerBinding(String key, TriggerSettings settings, int parameter, int mask) {
		this.key = key;
		this.settings = settings;
		this.parameter = parameter;
		this.mask = mask;
	}

	public boolean isToggle() {
		return mask != 0;
	}
}
