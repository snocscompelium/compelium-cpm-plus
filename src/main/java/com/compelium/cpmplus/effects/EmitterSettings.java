package com.compelium.cpmplus.effects;

import java.text.Normalizer;
import java.util.Locale;

import com.compelium.cpmplus.physics.TriggerType;

/**
 * A particle emitter, read from an animation name such as
 * {@code cplus_particula flame qtd=10 vel=1 esp=2}.
 *
 * <ul>
 * <li>The word after {@code cplus_particula} is the Minecraft particle id (flame, smoke, dust...).</li>
 * <li>qtd: particles per second (0 to 200). With the landing or jump trigger, particles per event.</li>
 * <li>vel: speed, 0 to 10.</li>
 * <li>esp: spread, 0 (a thin jet) to 10 (every direction).</li>
 * <li>cor: colour as RRGGBB, for coloured dust.</li>
 * <li>tam: size of coloured dust, 0.1 to 4.</li>
 * <li>gatilho: optional movement trigger that controls the amount (velocidade, queda, pouso...).</li>
 * </ul>
 */
public final class EmitterSettings {
	public static final String PREFIX = TriggerType.PREFIX + "particula";
	public static final String DEFAULT_PARTICLE = "flame";
	public static final float DEFAULT_QTD = 10;
	public static final float DEFAULT_VEL = 1;
	public static final float DEFAULT_ESP = 2;
	public static final int DEFAULT_COR = 0xFFFFFF;
	public static final float DEFAULT_TAM = 1;
	public static final float MAX_QTD = 200;
	/** Spaces and semicolons separate settings; so does a comma right before the next setting's name. */
	static final String SEPARATORS = "[\\s;]+|,(?=[a-z])";

	/** Blocks per tick for each step of the speed setting. */
	static final float SPEED_UNIT = 0.02f;
	/** Degrees of cone half-angle for each step of the spread setting. */
	static final float SPREAD_UNIT = 18f;

	public final String particle;
	public final float qtd, vel, esp, tam;
	public final int cor;
	/** Movement trigger that controls this emitter, or null to emit all the time. */
	public final TriggerType gatilho;

	/** Text Minecraft's particle parser understands. */
	public final String particleCommand;
	/** Particle speed in blocks per tick. */
	public final float speed;
	/** Half-angle of the emission cone, in radians. */
	public final float spreadRadians;

	private EmitterSettings(String particle, float qtd, float vel, float esp, int cor, float tam, TriggerType gatilho) {
		this.particle = particle;
		this.qtd = qtd;
		this.vel = vel;
		this.esp = esp;
		this.cor = cor & 0xFFFFFF;
		this.tam = tam;
		this.gatilho = gatilho;
		this.speed = vel * SPEED_UNIT;
		this.spreadRadians = (float) Math.toRadians(esp * SPREAD_UNIT);
		this.particleCommand = buildCommand(particle, this.cor, tam);
	}

	public static EmitterSettings of(String particle, float qtd, float vel, float esp, int cor, float tam, TriggerType gatilho) {
		String p = cleanParticle(particle);
		return new EmitterSettings(p == null ? DEFAULT_PARTICLE : p, clamp(qtd, 0, MAX_QTD), clamp(vel, 0, 10), clamp(esp, 0, 10), cor, clamp(tam, 0.1f, 4f), gatilho);
	}

	/** Returns null when the name is not a particle emitter. */
	public static EmitterSettings parse(String name) {
		if (name == null) return null;
		String n = normalize(name);
		if (!n.startsWith(PREFIX)) return null;
		String rest = n.substring(PREFIX.length());
		if (!rest.isEmpty() && !Character.isWhitespace(rest.charAt(0)) && rest.charAt(0) != ';') return null;

		String particle = DEFAULT_PARTICLE;
		float qtd = DEFAULT_QTD, vel = DEFAULT_VEL, esp = DEFAULT_ESP, tam = DEFAULT_TAM;
		int cor = DEFAULT_COR;
		TriggerType gatilho = null;
		boolean first = true;
		for (String t : rest.trim().split(SEPARATORS)) {
			if (t.isEmpty()) continue;
			int eq = t.indexOf('=');
			if (eq < 0) {
				if (first) {
					String p = cleanParticle(trimCommas(t));
					if (p != null) particle = p;
				}
				first = false;
				continue;
			}
			first = false;
			if (eq == 0 || eq == t.length() - 1) continue;
			String key = t.substring(0, eq);
			String value = trimCommas(t.substring(eq + 1));
			switch (key) {
			case "qtd":
			case "quantidade":
				qtd = number(value, qtd, 0, MAX_QTD);
				break;
			case "vel":
			case "velocidade":
				vel = number(value, vel, 0, 10);
				break;
			case "esp":
			case "espalhamento":
				esp = number(value, esp, 0, 10);
				break;
			case "tam":
			case "tamanho":
				tam = number(value, tam, 0.1f, 4f);
				break;
			case "cor":
				cor = color(value, cor);
				break;
			case "gatilho":
				gatilho = TriggerType.byId(value);
				break;
			default:
				break;
			}
		}
		return new EmitterSettings(particle, qtd, vel, esp, cor, tam, gatilho);
	}

	/** Builds the animation name, leaving out values that match the defaults. */
	public String toName() {
		StringBuilder sb = new StringBuilder(PREFIX).append(' ').append(particle);
		append(sb, "qtd", qtd, DEFAULT_QTD);
		append(sb, "vel", vel, DEFAULT_VEL);
		append(sb, "esp", esp, DEFAULT_ESP);
		if (usesColor()) {
			if (cor != DEFAULT_COR) sb.append(" cor=").append(String.format(Locale.ROOT, "%06x", cor));
			append(sb, "tam", tam, DEFAULT_TAM);
		}
		if (gatilho != null) sb.append(" gatilho=").append(gatilho.id);
		return sb.toString();
	}

	public boolean usesColor() {
		return isDust(particle);
	}

	/** True for landing and jump triggers, which fire a burst instead of a steady stream. */
	public boolean isBurst() {
		return gatilho == TriggerType.POUSO || gatilho == TriggerType.PULO;
	}

	static boolean isDust(String particle) {
		return "dust".equals(particle) || "minecraft:dust".equals(particle);
	}

	private static String buildCommand(String particle, int cor, float tam) {
		if (isDust(particle)) {
			float r = ((cor >> 16) & 0xFF) / 255f;
			float g = ((cor >> 8) & 0xFF) / 255f;
			float b = (cor & 0xFF) / 255f;
			return String.format(Locale.ROOT, "minecraft:dust{color:[%.3f,%.3f,%.3f],scale:%.2f}", r, g, b, tam);
		}
		return particle.indexOf(':') >= 0 ? particle : "minecraft:" + particle;
	}

	/** Keeps only characters a particle id can have; returns null when nothing is left. */
	private static String cleanParticle(String s) {
		if (s == null) return null;
		String p = normalize(s).replaceAll("[^a-z0-9_.:/-]", "");
		if (p.isEmpty() || p.startsWith(":") || p.endsWith(":")) return null;
		return p;
	}

	/** Commas are decimal marks (2,5) or a habit after a value (qtd=10,); both are fine. */
	static String trimCommas(String s) {
		int end = s.length();
		while (end > 0 && s.charAt(end - 1) == ',') end--;
		return s.substring(0, end);
	}

	private static float number(String s, float def, float min, float max) {
		try {
			float v = Float.parseFloat(s.replace(',', '.'));
			if (Float.isNaN(v) || Float.isInfinite(v)) return def;
			return clamp(v, min, max);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	static int color(String s, int def) {
		String h = s.startsWith("#") ? s.substring(1) : s;
		if (h.length() == 3) {
			h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
		}
		if (h.length() != 6) return def;
		try {
			return Integer.parseInt(h, 16);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static void append(StringBuilder sb, String key, float value, float def) {
		if (Math.abs(value - def) < 0.001f) return;
		sb.append(' ').append(key).append('=');
		if (Math.abs(value - Math.round(value)) < 0.001f) sb.append(Math.round(value));
		else sb.append(String.format(Locale.ROOT, "%.1f", value));
	}

	static String normalize(String s) {
		String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return n.toLowerCase(Locale.ROOT);
	}

	static float clamp(float v, float min, float max) {
		return v < min ? min : (v > max ? max : v);
	}
}
