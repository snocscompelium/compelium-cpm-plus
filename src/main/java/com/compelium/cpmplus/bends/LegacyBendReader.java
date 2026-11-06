package com.compelium.cpmplus.bends;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import com.tom.cpm.shared.animation.CustomPose;
import com.tom.cpm.shared.definition.ModelDefinition;

/**
 * Reads the bend markers from CPM's default animation format ({@code ModelPartAnimation}).
 * CPM still saves models in this format unless "experimental export" is turned on in its settings,
 * so this is what almost every model uses (the "test in game" button too).
 *
 * <p>The marker entries are removed from the part before CPM registers its animations,
 * so the marker never plays and never shows up as a gesture button.
 */
public final class LegacyBendReader {
	private LegacyBendReader() {}

	private static volatile boolean ready, disabled;
	private static Field parsedData, name, pose, components;

	/** Called at the start of {@code ModelPartAnimation.apply(ModelDefinition)}. */
	public static void onApply(Object part, ModelDefinition def) {
		if (disabled || part == null || !(def instanceof RigHolder) || !Bends.enabled()) return;
		try {
			if (!ready) init(part.getClass());
			Map<?, ?> data = (Map<?, ?>) parsedData.get(part);
			if (data == null || data.isEmpty()) return;
			RigCollector rc = new RigCollector();
			for (Iterator<? extends Map.Entry<?, ?>> it = data.entrySet().iterator(); it.hasNext();) {
				Object rd = it.next().getValue();
				if (rd == null) continue;
				BendName bn = BendName.parse(markerName(rd));
				if (bn == null) continue;
				rc.marker(bn);
				int[] ids = (int[]) components.get(rd);
				if (ids != null) {
					for (int id : ids) rc.add(bn, id);
				}
				it.remove();
			}
			rc.finish(def);
		} catch (Throwable t) {
			disabled = true;
			Bends.warn("Could not read the bends in CPM's default animation format; that part is off.", t);
		}
	}

	private static String markerName(Object rd) throws IllegalAccessException {
		Object n = name.get(rd);
		if (n instanceof String) return stripTypePrefix((String) n);
		Object p = pose.get(rd);
		if (p instanceof CustomPose) return ((CustomPose) p).getName();
		return null;
	}

	/** CPM stores the animation type in front of the name: {@code $layer$bend_left_arm}, {@code $value$...}. */
	static String stripTypePrefix(String n) {
		if (n.startsWith("$")) {
			int end = n.indexOf('$', 1);
			if (end > 0) return n.substring(end + 1);
		}
		return n;
	}

	private static synchronized void init(Class<?> partClass) throws ReflectiveOperationException {
		if (ready) return;
		Field data = field(partClass, "parsedData");
		Class<?> rdClass = Class.forName(partClass.getName() + "$ResolvedData", false, partClass.getClassLoader());
		Field n = field(rdClass, "name");
		Field p = field(rdClass, "pose");
		Field c = field(rdClass, "components");
		parsedData = data;
		name = n;
		pose = p;
		components = c;
		ready = true;
	}

	private static Field field(Class<?> c, String fieldName) throws NoSuchFieldException {
		for (Class<?> k = c; k != null; k = k.getSuperclass()) {
			try {
				Field f = k.getDeclaredField(fieldName);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException ignored) {
			}
		}
		throw new NoSuchFieldException(c.getName() + "." + fieldName);
	}
}
