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
 *
 * Some mods draw the world more than once per frame. Shaders (Iris) draw every entity again
 * from the sun's point of view to make shadows, with the sun's view baked into the drawing
 * position. Positions from that pass would land in the wrong place, so the shadow pass is
 * skipped, and any other pass drawn with a turned view has that view undone first.
 * Everything runs on the render thread.
 */
public final class RenderCapture {
	private static final int MAX_DEPTH = 16;
	private static final ArrayDeque<Frame> FRAMES = new ArrayDeque<>();
	private static final ArrayDeque<World> WORLDS = new ArrayDeque<>();
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

	/** Called when Minecraft starts drawing one entity in the world, with the normal view. */
	public static void enterWorld(double cameraX, double cameraY, double cameraZ) {
		enterWorld(cameraX, cameraY, cameraZ, null, false);
	}

	/**
	 * Called when Minecraft starts drawing one entity in the world.
	 *
	 * @param view       the drawing matrix at that moment (16 numbers, column by column);
	 *                   in the normal world pass it does nothing (identity)
	 * @param shadowPass true while a shader draws its shadows: nothing from this pass is used
	 */
	public static void enterWorld(double cameraX, double cameraY, double cameraZ, float[] view, boolean shadowPass) {
		if (WORLDS.size() >= MAX_DEPTH) WORLDS.clear();
		double[] undo = null;
		boolean skip = shadowPass;
		if (!skip && view != null && view.length >= 16 && !Mat.isIdentity(view, 1e-4)) {
			undo = Mat.invert(Mat.fromColumnMajor(view));
			// A view that can't be undone: better no particles than particles in the wrong place
			if (undo == null) skip = true;
		}
		WORLDS.push(new World(cameraX, cameraY, cameraZ, undo, skip));
	}

	public static void exitWorld() {
		WORLDS.poll();
		if (WORLDS.isEmpty()) FRAMES.clear();
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
		World w = WORLDS.peek();
		if (f == null || w == null || w.skip || columnMajor == null || columnMajor.length < 16) return;
		f.active = true;
		f.root = w.undo == null ? columnMajor : Mat.toFloat(Mat.mul(w.undo, Mat.fromColumnMajor(columnMajor)));
		f.camX = w.camX;
		f.camY = w.camY;
		f.camZ = w.camZ;
		f.owner = preparedOwner;
		f.motion = preparedMotion;
	}

	/** The frame of the entity being drawn now (for tests). */
	static Frame currentFrame() {
		return FRAMES.peek();
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
			if (sink.ready()) EmitterRuntime.emit(f, f.camX, f.camY, f.camZ, sink, System.nanoTime());
		} catch (Throwable e) {
			if (errors++ < 5) Log.warn("[Compelium's CPM+] Error while spawning particles", e);
		}
	}

	/** One entity draw in the world: where the camera is and how this pass draws. */
	static final class World {
		final double camX, camY, camZ;
		/** Undoes the view this pass draws with, or null for the normal view. */
		final double[] undo;
		final boolean skip;

		World(double camX, double camY, double camZ, double[] undo, boolean skip) {
			this.camX = camX;
			this.camY = camY;
			this.camZ = camZ;
			this.undo = undo;
			this.skip = skip;
		}
	}

	static final class Frame {
		boolean active;
		float[] root;
		double camX, camY, camZ;
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
