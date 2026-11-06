package com.compelium.cpmplus.bends;

/** Added to CPM's SerializedTrigger by a mixin: marks the triggers of bend marker animations. */
public interface TriggerMarker {
	boolean cpmplus$isMarker();

	void cpmplus$setMarker(boolean marker);
}
