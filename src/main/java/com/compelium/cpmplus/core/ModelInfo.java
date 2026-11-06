package com.compelium.cpmplus.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.compelium.cpmplus.effects.EmitterSettings;
import com.compelium.cpmplus.physics.LayerBinding;
import com.compelium.cpmplus.physics.TriggerSettings;
import com.compelium.cpmplus.physics.TriggerType;
import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.IAnimation;
import com.tom.cpm.shared.animation.IModelComponent;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RenderedCube;
import com.tom.cpm.shared.model.render.ItemRenderer;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;
import com.tom.cpm.shared.util.Log;

/**
 * Everything this mod found in one loaded model, read once and kept until the model unloads.
 */
public final class ModelInfo {
	public static final ModelInfo EMPTY = new ModelInfo(Collections.<LayerBinding>emptyList(), new IdentityHashMap<>());
	private static final Map<ModelDefinition, ModelInfo> CACHE = new WeakHashMap<>();

	/** Movement animations and the CPM values they drive. */
	public final List<LayerBinding> physics;
	/** Particle emitters, keyed by the marker part CPM reports positions for. */
	public final IdentityHashMap<ItemRenderer, EmitterSettings[]> emitters;
	private final boolean gated;

	private ModelInfo(List<LayerBinding> physics, IdentityHashMap<ItemRenderer, EmitterSettings[]> emitters) {
		this.physics = physics;
		this.emitters = emitters;
		boolean g = false;
		for (EmitterSettings[] list : emitters.values()) {
			for (EmitterSettings e : list) {
				if (e.gatilho != null) g = true;
			}
		}
		this.gated = g;
	}

	public boolean isEmpty() {
		return physics.isEmpty() && emitters.isEmpty();
	}

	/** True when this model needs the player's movement to be tracked. */
	public boolean needsMotion() {
		return !physics.isEmpty() || gated;
	}

	public static ModelInfo of(ModelDefinition def) {
		ModelInfo info = CACHE.get(def);
		if (info == null) {
			info = scan(def);
			CACHE.put(def, info);
		}
		return info;
	}

	private static ModelInfo scan(ModelDefinition def) {
		List<LayerBinding> physics = new ArrayList<>();
		IdentityHashMap<ItemRenderer, EmitterSettings[]> emitters = new IdentityHashMap<>();
		try {
			AnimationRegistry reg = def.getAnimations();
			for (AbstractGestureButtonData btn : reg.getNamedActions()) {
				String name = btn.getName();
				if (name == null) continue;

				TriggerSettings st = TriggerSettings.parse(name);
				if (st != null) {
					addPhysics(physics, btn, name, st);
					continue;
				}

				EmitterSettings em = EmitterSettings.parse(name);
				if (em != null) {
					int found = 0;
					for (IAnimation anim : CpmAccess.animationsFor(reg, btn)) {
						for (IModelComponent c : CpmAccess.components(anim, def)) {
							if (c instanceof RenderedCube && ((RenderedCube) c).itemRenderer != null) {
								addEmitter(emitters, ((RenderedCube) c).itemRenderer, em);
								found++;
							}
						}
					}
					if (found == 0) Log.warn("[Compelium's CPM+] Particle emitter '" + name + "' has no marker point; create it with the CPM+ particle assistant");
					continue;
				}

				if (TriggerSettings.normalize(name).startsWith(TriggerType.PREFIX)) {
					Log.warn("[Compelium's CPM+] Unknown name in model: '" + name + "'");
				}
			}
		} catch (Throwable e) {
			Log.warn("[Compelium's CPM+] Could not read the model's animations", e);
			return EMPTY;
		}
		if (physics.isEmpty() && emitters.isEmpty()) return EMPTY;
		Log.info("[Compelium's CPM+] Found " + physics.size() + " movement trigger(s) and " + emitters.size() + " particle marker(s) in a model");
		return new ModelInfo(physics.isEmpty() ? Collections.<LayerBinding>emptyList() : Collections.unmodifiableList(physics), emitters);
	}

	private static void addPhysics(List<LayerBinding> out, AbstractGestureButtonData btn, String name, TriggerSettings st) {
		if (btn instanceof ValueParameterButtonData) {
			out.add(new LayerBinding(name, st, ((ValueParameterButtonData) btn).parameter, 0));
		} else if (btn instanceof BoolParameterToggleButtonData) {
			BoolParameterToggleButtonData t = (BoolParameterToggleButtonData) btn;
			if (t.mask != 0) out.add(new LayerBinding(name, st, t.parameter, t.mask));
		} else {
			Log.warn("[Compelium's CPM+] '" + name + "' must be a Value layer or a Layer animation to work as a trigger");
		}
	}

	private static void addEmitter(IdentityHashMap<ItemRenderer, EmitterSettings[]> map, ItemRenderer r, EmitterSettings em) {
		EmitterSettings[] old = map.get(r);
		if (old == null) {
			map.put(r, new EmitterSettings[] {em});
			return;
		}
		for (EmitterSettings e : old) {
			if (e == em) return;
		}
		EmitterSettings[] grown = new EmitterSettings[old.length + 1];
		System.arraycopy(old, 0, grown, 0, old.length);
		grown[old.length] = em;
		map.put(r, grown);
	}
}
