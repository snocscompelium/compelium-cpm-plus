package com.compelium.cpmplus.effects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.compelium.cpmplus.core.ModelInfo;
import com.compelium.cpmplus.physics.PlayerMotion;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.util.Log;

/**
 * Collects where each particle marker is while CPM draws a player, then hands the
 * positions to the emitters once the player is fully drawn.
 *
 * CPM reports every marker's position relative to the model's root. The Minecraft-specific
 * hooks add the two missing pieces: the root's position for this frame and the camera
 * position, and they only open a frame for players drawn in the world (not in menus).
 * Everything runs on the render thread.
 */
public final class RenderCapture {
	private static final int MAX_DEPTH = 16;
	private static final ArrayDeque<Frame> FRAMES = new ArrayDeque<>();
	private static int worldDepth;
	private static double camX, camY, camZ;
	private static Object preparedOwner;
	private static PlayerMotion preparedMotion;
	private static int errors;

	private RenderCapture() {
	}

	/** Called by the movement code each time CPM prepares a player for drawing. */
	public static void preparedPlayer(Object owner, PlayerMotion motion) {
		preparedOwner = owner;
		preparedMotion = motion;
	}

	/** Called when Minecraft starts drawing one entity in the world. */
	public static void enterWorld(double cameraX, double cameraY, double cameraZ) {
		worldDepth++;
		camX = cameraX;
		camY = cameraY;
		camZ = cameraZ;
	}

	public static void exitWorld() {
		if (worldDepth > 0) worldDepth--;
		if (worldDepth == 0) FRAMES.clear();
	}

	/** Called at the start of every living entity draw, in the world or in a menu. */
	public static void beginEntity() {
		if (FRAMES.size() >= MAX_DEPTH) FRAMES.clear();
		FRAMES.push(new Frame());
	}

	/**
	 * Called right before the entity's model is drawn, with the pose of the model's root
	 * (16 numbers, column by column).
	 */
	public static void modelRoot(float[] columnMajor) {
		Frame f = FRAMES.peek();
		if (f == null || worldDepth <= 0 || columnMajor == null || columnMajor.length < 16) return;
		f.active = true;
		f.root = columnMajor;
		f.owner = preparedOwner;
		f.motion = preparedMotion;
	}

	/** Called by CPM (through a hook) for every marker part it draws. */
	public static void onStoreTransform(ModelDefinition def, ItemRenderer renderer, MatrixStack stack, boolean doRender) {
		Frame f = FRAMES.peek();
		if (f == null || !f.active || renderer == null || stack == null) return;
		try {
			EmitterSettings[] emitters = ModelInfo.of(def).emitters.get(renderer);
			if (emitters == null) return;
			if (f.items == null) f.items = new ArrayList<>(4);
			f.items.add(new Captured(renderer, emitters, stack.getLast().getMatrix().toArray(), doRender));
		} catch (Throwable e) {
			if (errors++ < 5) Log.warn("[Compelium's CPM+] Could not read a marker position", e);
		}
	}

	/** Called when the entity is fully drawn. */
	public static void endEntity(ParticleSink sink) {
		Frame f = FRAMES.poll();
		if (f == null || !f.active || f.items == null || sink == null) return;
		try {
			if (sink.ready()) EmitterRuntime.emit(f, camX, camY, camZ, sink, System.nanoTime());
		} catch (Throwable e) {
			if (errors++ < 5) Log.warn("[Compelium's CPM+] Error while spawning particles", e);
		}
	}

	static final class Frame {
		boolean active;
		float[] root;
		Object owner;
		PlayerMotion motion;
		List<Captured> items;
	}

	static final class Captured {
		final ItemRenderer renderer;
		final EmitterSettings[] emitters;
		/** CPM matrix, row by row. */
		final float[] local;
		final boolean visible;

		Captured(ItemRenderer renderer, EmitterSettings[] emitters, float[] local, boolean visible) {
			this.renderer = renderer;
			this.emitters = emitters;
			this.local = local;
			this.visible = visible;
		}
	}
}
