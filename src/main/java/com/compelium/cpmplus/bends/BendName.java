package com.compelium.cpmplus.bends;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the special animation names that mark which CPM pieces should bend.
 *
 * <pre>
 *   bend_left_arm      dobra_braco_esquerdo     elbow, left arm
 *   bend_right_arm     dobra_braco_direito      elbow, right arm
 *   bend_left_leg      dobra_perna_esquerda     knee, left leg
 *   bend_right_leg     dobra_perna_direita      knee, right leg
 *   bend_body          dobra_corpo              waist (upper torso turns)
 * </pre>
 *
 * An optional number at the end is a percentage of the bend, e.g. {@code bend_left_arm_50}
 * turns the piece by half the emote's angle (useful for an elbow "joint" filler cube).
 * Case, accents, spaces and dashes are ignored. CPM's own "#suffix" is already removed
 * by CPM before the name reaches us.
 */
public final class BendName {
	private static final Map<String, BendPart> NAMES = new HashMap<>();

	static {
		alias(BendPart.LEFT_ARM, "bend_left_arm", "bend_leftarm", "bend_arm_left", "dobra_braco_esquerdo", "dobrar_braco_esquerdo");
		alias(BendPart.RIGHT_ARM, "bend_right_arm", "bend_rightarm", "bend_arm_right", "dobra_braco_direito", "dobrar_braco_direito");
		alias(BendPart.LEFT_LEG, "bend_left_leg", "bend_leftleg", "bend_leg_left", "dobra_perna_esquerda", "dobrar_perna_esquerda");
		alias(BendPart.RIGHT_LEG, "bend_right_leg", "bend_rightleg", "bend_leg_right", "dobra_perna_direita", "dobrar_perna_direita");
		alias(BendPart.TORSO, "bend_body", "bend_torso", "dobra_corpo", "dobra_tronco", "dobrar_corpo", "dobrar_tronco");
	}

	private static void alias(BendPart part, String... names) {
		for (String n : names) NAMES.put(n, part);
	}

	public final BendPart part;
	/** 1.0 = follow the emote exactly. */
	public final float factor;
	/** Name as written by the modeler (for log messages). */
	public final String original;

	private BendName(BendPart part, float factor, String original) {
		this.part = part;
		this.factor = factor;
		this.original = original;
	}

	/** The usual marker name for a body part, with the percentage at the end when it is not 100. */
	public static String nameFor(BendPart part, int percent) {
		String base;
		switch (part) {
		case LEFT_ARM: base = "bend_left_arm"; break;
		case RIGHT_ARM: base = "bend_right_arm"; break;
		case LEFT_LEG: base = "bend_left_leg"; break;
		case RIGHT_LEG: base = "bend_right_leg"; break;
		default: base = "bend_body"; break;
		}
		return percent == 100 ? base : base + "_" + percent;
	}

	public String groupKey() {
		return part.name() + "@" + factor;
	}

	public static String normalize(String name) {
		String n = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		n = n.toLowerCase(Locale.ROOT).trim();
		n = n.replaceAll("[\\s\\-.]+", "_");
		n = n.replaceAll("_+", "_");
		while (n.startsWith("_")) n = n.substring(1);
		while (n.endsWith("_")) n = n.substring(0, n.length() - 1);
		return n;
	}

	/** @return the parsed marker, or null when the name is not a bend marker. */
	public static BendName parse(String name) {
		if (name == null || name.isEmpty()) return null;
		String n = normalize(name);
		BendPart part = NAMES.get(n);
		float factor = 1f;
		if (part == null) {
			int i = n.lastIndexOf('_');
			if (i > 0 && i < n.length() - 1) {
				String num = n.substring(i + 1);
				if (num.matches("\\d{1,3}")) {
					part = NAMES.get(n.substring(0, i));
					int pct = Integer.parseInt(num);
					if (pct <= 0 || pct > 400) part = null;
					factor = pct / 100f;
				}
			}
		}
		if (part == null) return null;
		return new BendName(part, factor, name);
	}
}
