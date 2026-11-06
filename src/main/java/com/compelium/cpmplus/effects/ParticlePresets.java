package com.compelium.cpmplus.effects;

/**
 * The particles offered in the editor's list. Any other Minecraft particle id can still be typed by hand.
 */
public final class ParticlePresets {
	public static final String[] IDS = {
			"flame",
			"soul_fire_flame",
			"small_flame",
			"smoke",
			"large_smoke",
			"white_smoke",
			"campfire_cosy_smoke",
			"end_rod",
			"enchant",
			"witch",
			"dust",
			"electric_spark",
			"glow",
			"heart",
			"note",
			"portal",
			"snowflake",
			"cherry_leaves",
			"white_ash",
			"happy_villager",
			"crit",
			"enchanted_crit",
			"totem_of_undying",
			"soul",
			"cloud",
			"poof",
			"firework",
			"dripping_water",
	};

	private ParticlePresets() {
	}

	public static String langKey(String id) {
		return "cpmplus.particle." + id;
	}
}
