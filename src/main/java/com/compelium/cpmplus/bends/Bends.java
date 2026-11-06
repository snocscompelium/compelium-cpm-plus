package com.compelium.cpmplus.bends;

import com.tom.cpm.shared.util.Log;

/**
 * Emote bends: Emotecraft's elbow, knee and waist bends on CPM models.
 *
 * This is the "CPM Emote Bends" mod, now part of Compelium's CPM+. The modeler marks the
 * pieces that bend with an animation named like {@code bend_left_arm}; the same names
 * keep working, so models made for that mod need no changes.
 *
 * Like the rest of the mod's core, this code only touches CPM's and the animation
 * libraries' classes, never Minecraft's, so it works on every Minecraft version.
 */
public final class Bends {
	private static final String PREFIX = "[Compelium's CPM+] ";
	/** Mod id of the stand-alone mod this code came from. */
	public static final String OLD_MOD_ID = "cpm_emote_bends";

	private static volatile Boolean enabled;

	private Bends() {
	}

	/**
	 * False when the stand-alone CPM Emote Bends mod is installed too: then it does the
	 * bends and this copy stays out of the way, so nothing bends twice.
	 */
	public static boolean enabled() {
		Boolean e = enabled;
		if (e == null) {
			boolean old;
			try {
				old = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(OLD_MOD_ID);
			} catch (Throwable t) {
				old = false;
			}
			e = !old;
			enabled = e;
			if (old) info("CPM Emote Bends is installed too, so it handles the emote bends. It is no longer needed: you can remove it from the mods folder, Compelium's CPM+ does the same thing.");
		}
		return e;
	}

	public static void info(String msg) {
		try {
			Log.info(PREFIX + msg);
		} catch (Throwable t) {
			System.out.println(PREFIX + msg);
		}
	}

	public static void debug(String msg) {
		try {
			Log.debug(PREFIX + msg);
		} catch (Throwable t) {
			// debug output is optional
		}
	}

	public static void warn(String msg) {
		try {
			Log.warn(PREFIX + msg);
		} catch (Throwable t) {
			System.out.println(PREFIX + "WARN " + msg);
		}
	}

	public static void warn(String msg, Throwable error) {
		try {
			Log.warn(PREFIX + msg, error);
		} catch (Throwable t) {
			System.out.println(PREFIX + "WARN " + msg);
			error.printStackTrace();
		}
	}
}
