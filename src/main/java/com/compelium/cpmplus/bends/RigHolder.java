package com.compelium.cpmplus.bends;

/** Added to CPM's ModelDefinition by a mixin: stores the bend rig found in that model. */
public interface RigHolder {
	/** Set while the model loads: at least one bend marker animation was found (even if it had no pieces). */
	int FLAG_MARKERS_SEEN = 1;
	/** Set on the render thread after the model's bend status was written to the log. */
	int FLAG_REPORTED = 2;

	BendRig cpmplus$getRig();

	void cpmplus$setRig(BendRig rig);

	int cpmplus$getFlags();

	void cpmplus$setFlags(int flags);

	/** Adds the pieces found in one animation part of the model (a model can have several). */
	static void addRig(Object def, BendRig rig, boolean markersSeen) {
		if (!(def instanceof RigHolder)) return;
		RigHolder h = (RigHolder) def;
		if (markersSeen) h.cpmplus$setFlags(h.cpmplus$getFlags() | FLAG_MARKERS_SEEN);
		if (rig != null) h.cpmplus$setRig(BendRig.merge(h.cpmplus$getRig(), rig));
	}
}
