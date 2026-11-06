package com.compelium.cpmplus.physics;

import java.text.Normalizer;
import java.util.Locale;

/**
 * A trigger plus its settings, read from an animation name such as
 * {@code cplus_queda peso=4 mola=6}.
 *
 * All settings use a 0 to 10 scale so they are easy to remember:
 * <ul>
 * <li>peso: how slowly the part reacts (0 = instant, 10 = very slow)</li>
 * <li>mola: how much it swings past the target before settling (0 = none, 10 = a lot)</li>
 * <li>forca: how sensitive the trigger is (5 = normal, 10 = reaches the end twice as fast)</li>
 * <li>limite: the furthest the animation goes (10 = the whole animation)</li>
 * <li>limiar: for on/off layers, how strong the movement must be to switch on (5 = halfway)</li>
 * </ul>
 */
public final class TriggerSettings {
	public static final float DEFAULT_PESO = 3;
	public static final float DEFAULT_MOLA = 2;
	public static final float DEFAULT_FORCA = 5;
	public static final float DEFAULT_LIMITE = 10;
	public static final float DEFAULT_LIMIAR = 5;

	public final TriggerType type;
	public final float peso, mola, forca, limite, limiar;

	/** Spring stiffness in radians per second. */
	public final float omega;
	/** Damping ratio; 1 means no overshoot. */
	public final float zeta;
	/** Multiplier applied to the raw movement. */
	public final float gain;
	/** Largest output distance from rest, 0 to 1. */
	public final float limit;
	/** Switch-on point for on/off layers, 0 to 1. */
	public final float threshold;

	private TriggerSettings(TriggerType type, float peso, float mola, float forca, float limite, float limiar) {
		this.type = type;
		this.peso = peso;
		this.mola = mola;
		this.forca = forca;
		this.limite = limite;
		this.limiar = limiar;
		this.omega = 30f / (1f + 0.9f * peso);
		this.zeta = Math.max(0.08f, 1f - 0.09f * mola);
		this.gain = Math.max(0.02f, forca / 5f);
		this.limit = limite / 10f;
		this.threshold = Math.max(0.01f, limiar / 10f);
	}

	public static TriggerSettings defaults(TriggerType type) {
		return new TriggerSettings(type, DEFAULT_PESO, DEFAULT_MOLA, DEFAULT_FORCA, DEFAULT_LIMITE, DEFAULT_LIMIAR);
	}

	/**
	 * Reads an animation name. Returns null when the name does not belong to this mod
	 * or names a trigger that does not exist.
	 */
	public static TriggerSettings parse(String name) {
		if (name == null) return null;
		String n = normalize(name);
		if (!n.startsWith(TriggerType.PREFIX)) return null;
		String[] tokens = n.split("[\\s;]+|,(?=[a-z])");
		if (tokens.length == 0) return null;
		String first = tokens[0];
		while (first.endsWith(",")) first = first.substring(0, first.length() - 1);
		TriggerType type = TriggerType.byId(first.substring(TriggerType.PREFIX.length()));
		if (type == null) return null;

		float peso = DEFAULT_PESO, mola = DEFAULT_MOLA, forca = DEFAULT_FORCA, limite = DEFAULT_LIMITE, limiar = DEFAULT_LIMIAR;
		for (int i = 1; i < tokens.length; i++) {
			String t = tokens[i];
			int eq = t.indexOf('=');
			if (eq <= 0 || eq == t.length() - 1) continue;
			String key = t.substring(0, eq);
			float value;
			try {
				String raw = t.substring(eq + 1);
				while (raw.endsWith(",")) raw = raw.substring(0, raw.length() - 1);
				value = Float.parseFloat(raw.replace(',', '.'));
			} catch (NumberFormatException e) {
				continue;
			}
			if (Float.isNaN(value) || Float.isInfinite(value)) continue;
			value = clamp(value, 0, 10);
			switch (key) {
			case "peso": peso = value; break;
			case "mola": mola = value; break;
			case "forca": forca = value; break;
			case "limite": limite = value; break;
			case "limiar": limiar = value; break;
			default: break;
			}
		}
		return new TriggerSettings(type, peso, mola, forca, limite, limiar);
	}

	/** Builds the animation name for these settings, leaving out values that match the defaults. */
	public String toName() {
		StringBuilder sb = new StringBuilder(type.fullName());
		append(sb, "peso", peso, DEFAULT_PESO);
		append(sb, "mola", mola, DEFAULT_MOLA);
		append(sb, "forca", forca, DEFAULT_FORCA);
		append(sb, "limite", limite, DEFAULT_LIMITE);
		append(sb, "limiar", limiar, DEFAULT_LIMIAR);
		return sb.toString();
	}

	public static String nameFor(TriggerType type, float peso, float mola, float forca, float limite, float limiar) {
		return new TriggerSettings(type, clamp(peso, 0, 10), clamp(mola, 0, 10), clamp(forca, 0, 10), clamp(limite, 0, 10), clamp(limiar, 0, 10)).toName();
	}

	private static void append(StringBuilder sb, String key, float value, float def) {
		if (Math.abs(value - def) < 0.001f) return;
		sb.append(' ').append(key).append('=');
		if (Math.abs(value - Math.round(value)) < 0.001f) sb.append(Math.round(value));
		else sb.append(String.format(Locale.ROOT, "%.1f", value));
	}

	public static String normalize(String s) {
		String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return n.toLowerCase(Locale.ROOT);
	}

	public static float clamp(float v, float min, float max) {
		return v < min ? min : (v > max ? max : v);
	}
}
