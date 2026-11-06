package com.compelium.cpmplus.mc;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compelium.cpmplus.effects.ParticleSink;
import com.mojang.brigadier.StringReader;
import com.tom.cpm.shared.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;

/**
 * Spawns particles in the client world. Particle texts are read once and kept,
 * so a busy emitter costs nothing more than Minecraft's own particles.
 */
public final class McParticleSink implements ParticleSink {
	public static final McParticleSink INSTANCE = new McParticleSink();

	private final Map<String, ParticleOptions> cache = new HashMap<>();
	private final Set<String> invalid = new HashSet<>();
	private WeakReference<ClientLevel> cachedFor = new WeakReference<>(null);

	private McParticleSink() {
	}

	@Override
	public boolean ready() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null && !mc.isPaused();
	}

	@Override
	public void spawn(String particleCommand, double x, double y, double z, double vx, double vy, double vz) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		if (cachedFor.get() != level) {
			cache.clear();
			invalid.clear();
			cachedFor = new WeakReference<>(level);
		}
		ParticleOptions options = cache.get(particleCommand);
		if (options == null) {
			if (invalid.contains(particleCommand)) return;
			try {
				options = ParticleArgument.readParticle(new StringReader(particleCommand), level.registryAccess());
			} catch (Exception e) {
				invalid.add(particleCommand);
				Log.warn("[Compelium's CPM+] Unknown particle '" + particleCommand + "': " + e.getMessage());
				return;
			}
			cache.put(particleCommand, options);
		}
		level.addParticle(options, x, y, z, vx, vy, vz);
	}
}
