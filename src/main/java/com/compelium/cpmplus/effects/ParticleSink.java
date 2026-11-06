package com.compelium.cpmplus.effects;

/**
 * Where particles end up. The Minecraft-specific part of the mod provides the real one;
 * keeping it behind this small interface lets everything else work on any Minecraft version.
 */
public interface ParticleSink {
	/** False while the game is paused or no world is loaded. */
	boolean ready();

	/** Position in world coordinates, speed in blocks per tick. */
	void spawn(String particleCommand, double x, double y, double z, double vx, double vy, double vz);
}
