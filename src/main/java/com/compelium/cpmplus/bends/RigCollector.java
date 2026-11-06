package com.compelium.cpmplus.bends;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tom.cpm.shared.definition.ModelDefinition;

/** Gathers the pieces of the bend marker animations of one model while it loads. */
final class RigCollector {
	private final Map<String, BendName> names = new LinkedHashMap<>();
	private final Map<String, Set<Integer>> cubes = new LinkedHashMap<>();

	/** Registers a marker animation (it may turn out to have no pieces). */
	void marker(BendName bn) {
		names.putIfAbsent(bn.groupKey(), bn);
		cubes.computeIfAbsent(bn.groupKey(), k -> new LinkedHashSet<>());
	}

	void add(BendName bn, int cubeId) {
		marker(bn);
		cubes.get(bn.groupKey()).add(cubeId);
	}

	boolean hasMarkers() {
		return !names.isEmpty();
	}

	/** Builds the rig, writes what was found to the log and stores it in the model. */
	void finish(ModelDefinition def) {
		if (names.isEmpty()) return;
		List<BendRig.Group> groups = new ArrayList<>();
		StringBuilder found = new StringBuilder();
		StringBuilder empty = new StringBuilder();
		for (Map.Entry<String, Set<Integer>> e : cubes.entrySet()) {
			BendName bn = names.get(e.getKey());
			if (e.getValue().isEmpty()) {
				if (empty.length() > 0) empty.append(", ");
				empty.append(bn.original);
				continue;
			}
			int[] ids = e.getValue().stream().mapToInt(Integer::intValue).toArray();
			groups.add(new BendRig.Group(bn.part, bn.factor, bn.original, ids));
			if (found.length() > 0) found.append(", ");
			found.append(bn.original).append(" (").append(ids.length).append(ids.length == 1 ? " piece" : " pieces").append(')');
		}
		boolean local = isLocalPlayer(def);
		if (empty.length() > 0) {
			String msg = "The emote bend animation " + empty + " has no pieces. "
					+ "Inside that animation, move the piece that should bend (turn it a little, for example) so it gets registered.";
			if (local) Bends.warn(msg);
			else Bends.debug(msg);
		}
		if (found.length() > 0) {
			String msg = "Emote bends found in the model: " + found;
			if (local) Bends.info(msg);
			else Bends.debug(msg);
		}
		RigHolder.addRig(def, groups.isEmpty() ? null : new BendRig(groups), true);
	}

	static boolean isLocalPlayer(ModelDefinition def) {
		try {
			return def.getPlayerObj() != null && def.getPlayerObj().isClientPlayer();
		} catch (Throwable t) {
			return false;
		}
	}
}
