package com.compelium.cpmplus.bends;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tom.cpl.math.Rotation;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.PartRoot;
import com.tom.cpm.shared.model.PlayerModelParts;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.RootModelElement;

/**
 * The pieces of one CPM model that follow emote bends, grouped by body part and percentage.
 */
public final class BendRig {
	/** Waist pivot (in model pixels) used by playerAnimator when the head and arms follow a torso bend. */
	private static final float UPPER_PIVOT_Y = 6f;
	private static final PlayerModelParts[] UPPER_PARTS = {PlayerModelParts.HEAD, PlayerModelParts.LEFT_ARM, PlayerModelParts.RIGHT_ARM};

	public static final class Group {
		public final BendPart part;
		public final float factor;
		public final String label;
		final int[] cubeIds;
		private RenderedCube[] cubes;

		Group(BendPart part, float factor, String label, int[] cubeIds) {
			this.part = part;
			this.factor = factor;
			this.label = label;
			this.cubeIds = cubeIds;
		}

		public int size() {
			return cubeIds.length;
		}

		String key() {
			return part.name() + "@" + factor;
		}

		static Group union(Group a, Group b) {
			Set<Integer> ids = new LinkedHashSet<>();
			for (int id : a.cubeIds) ids.add(id);
			for (int id : b.cubeIds) ids.add(id);
			return new Group(a.part, a.factor, a.label, ids.stream().mapToInt(Integer::intValue).toArray());
		}

		RenderedCube[] resolve(ModelDefinition def) {
			RenderedCube[] r = cubes;
			if (r != null) return r;
			Set<RenderedCube> found = new HashSet<>();
			for (int id : cubeIds) {
				RenderedCube c = def.getElementById(id);
				if (c == null || c instanceof RootModelElement) continue;
				found.add(c);
			}
			List<RenderedCube> out = new ArrayList<>();
			for (RenderedCube c : found) {
				// A child of a piece that already bends follows its parent: don't turn it twice.
				boolean nested = false;
				for (RenderedCube p = c.getParent(); p != null; p = p.getParent()) {
					if (found.contains(p)) {
						nested = true;
						break;
					}
				}
				if (!nested) out.add(c);
			}
			r = out.toArray(new RenderedCube[0]);
			cubes = r;
			return r;
		}
	}

	private final Group[] groups;
	private final boolean hasTorso;

	BendRig(List<Group> groups) {
		this.groups = groups.toArray(new Group[0]);
		boolean t = false;
		for (Group g : groups) if (g.part.isTorso()) t = true;
		this.hasTorso = t;
	}

	public Group[] groups() {
		return groups;
	}

	/** Joins the pieces found in different animation parts of the same model. */
	static BendRig merge(BendRig a, BendRig b) {
		if (a == null) return b;
		if (b == null) return a;
		Map<String, Group> byKey = new LinkedHashMap<>();
		for (Group g : a.groups) byKey.merge(g.key(), g, Group::union);
		for (Group g : b.groups) byKey.merge(g.key(), g, Group::union);
		return new BendRig(new ArrayList<>(byKey.values()));
	}

	/**
	 * @param mainPass true for the player model itself; false for armor/elytra/cape sub-models,
	 *                 which pose again but must not move the head and arm roots a second time.
	 */
	public void apply(ModelDefinition def, BendFrame frame, boolean mainPass) {
		for (Group g : groups) {
			int i = g.part.ordinal();
			double angle = frame.bend[i] * g.factor;
			if (Math.abs(angle) < 1e-5) continue;
			double a = frame.axis[i];
			// Bend axis in the limb's own space. Limbs: (cos a, 0, sin a); torso: (cos a, 0, -sin a).
			double ux = Math.cos(a);
			double uz = g.part.isTorso() ? -Math.sin(a) : Math.sin(a);
			for (RenderedCube c : g.resolve(def)) {
				bendCube(c, ux, 0, uz, angle);
			}
		}
		if (mainPass && hasTorso && Math.abs(frame.upperBend) > 1e-5) {
			followTorso(def, frame.upperAxis, frame.upperBend);
		}
	}

	/** Turns a piece around its own pivot, around the limb's bend axis. */
	static void bendCube(RenderedCube c, double ux, double uy, double uz, double angle) {
		Rotation rot = c.rotation;
		if (rot == null) return;
		// Rotation of the chain between the root (the vanilla part) and this piece's parent.
		List<RenderedCube> chain = new ArrayList<>(4);
		RenderedCube p = c.getParent();
		while (p != null && !(p instanceof RootModelElement)) {
			chain.add(p);
			p = p.getParent();
		}
		if (p == null) return; // not attached to a body part
		double ax = ux, ay = uy, az = uz;
		if (!chain.isEmpty()) {
			Quat parent = Quat.identity();
			for (int k = chain.size() - 1; k >= 0; k--) {
				Rotation r = chain.get(k).rotation;
				if (r != null) parent = parent.mul(Quat.fromEulerZYX(r.x, r.y, r.z));
			}
			double[] v = parent.conj().rotate(ux, uy, uz);
			ax = v[0];
			ay = v[1];
			az = v[2];
		}
		boolean pureX = Math.abs(ay) < 1e-6 && Math.abs(az) < 1e-6;
		if (pureX && Math.abs(rot.y) < 1e-6 && Math.abs(rot.z) < 1e-6) {
			// Common case: keep the numbers simple (no Euler re-decomposition).
			rot.x += (float) (ax > 0 ? angle : -angle);
			return;
		}
		Quat q = Quat.axisAngle(ax, ay, az, angle).mul(Quat.fromEulerZYX(rot.x, rot.y, rot.z));
		double[] e = q.toEulerZYX();
		rot.x = (float) e[0];
		rot.y = (float) e[1];
		rot.z = (float) e[2];
	}

	/** playerAnimator: the head and arms follow the upper torso, like vanilla playerAnimator did. */
	private static void followTorso(ModelDefinition def, double axis, double angle) {
		double ux = Math.cos(-axis), uz = Math.sin(-axis);
		Quat q = Quat.axisAngle(ux, 0, uz, angle);
		for (PlayerModelParts part : UPPER_PARTS) {
			PartRoot root = def.getModelElementFor(part);
			if (root == null) continue;
			root.forEach(e -> moveRoot(e, q));
		}
	}

	private static void moveRoot(RootModelElement e, Quat q) {
		Vec3f pos = e.defPos;
		Rotation rot = e.defRot;
		if (pos == null || rot == null) return;
		double[] v = q.rotate(pos.x, pos.y - UPPER_PIVOT_Y, pos.z);
		pos.x = (float) v[0];
		pos.y = (float) (v[1] + UPPER_PIVOT_Y);
		pos.z = (float) v[2];
		double[] r = q.mul(Quat.fromEulerZYX(rot.x, rot.y, rot.z)).toEulerZYX();
		rot.x = (float) r[0];
		rot.y = (float) r[1];
		rot.z = (float) r[2];
	}
}
