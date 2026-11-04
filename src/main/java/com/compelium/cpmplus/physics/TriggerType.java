package com.compelium.cpmplus.physics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Every physics trigger the mod understands.
 *
 * One-way triggers go from 0 (rest) to 1 (strongest).
 * Two-way triggers rest at 0.5 and go to 0 or 1 depending on the direction,
 * so a value layer with three frames (one side, rest, other side) can swing both ways.
 */
public enum TriggerType {
	QUEDA("queda", false),
	SUBIDA("subida", false),
	FRENTE("frente", false),
	TRAS("tras", false),
	ESQUERDA("esquerda", false),
	DIREITA("direita", false),
	VELOCIDADE("velocidade", false),
	GIRO_ESQUERDA("giro_esquerda", false),
	GIRO_DIREITA("giro_direita", false),
	POUSO("pouso", false),
	PULO("pulo", false),
	AGUA("agua", false),
	PARADO("parado", false),

	VERTICAL("vertical", true),
	FRENTE_TRAS("frente_tras", true),
	LADOS("lados", true),
	GIRO("giro", true),
	;

	public static final String PREFIX = "cplus_";
	public static final TriggerType[] VALUES = values();
	private static final Map<String, TriggerType> BY_ID = new HashMap<>();

	static {
		for (TriggerType t : VALUES) BY_ID.put(t.id, t);
		// Friendly spellings people are likely to type
		BY_ID.put("caindo", QUEDA);
		BY_ID.put("subindo", SUBIDA);
		BY_ID.put("andar", FRENTE);
		BY_ID.put("para_tras", TRAS);
		BY_ID.put("lado_esquerdo", ESQUERDA);
		BY_ID.put("lado_direito", DIREITA);
		BY_ID.put("correr", VELOCIDADE);
		BY_ID.put("impacto", POUSO);
		BY_ID.put("pular", PULO);
		BY_ID.put("nadando", AGUA);
		BY_ID.put("frentetras", FRENTE_TRAS);
		BY_ID.put("lado", LADOS);
	}

	public final String id;
	public final boolean twoWay;

	TriggerType(String id, boolean twoWay) {
		this.id = id;
		this.twoWay = twoWay;
	}

	/** Full animation name for this trigger, as typed in the CPM editor. */
	public String fullName() {
		return PREFIX + id;
	}

	public String langKey() {
		return "cpmplus.trigger." + id;
	}

	public static TriggerType byId(String id) {
		return BY_ID.get(id.toLowerCase(Locale.ROOT));
	}
}
