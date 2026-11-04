package com.compelium.cpmplus.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.anim.menu.AbstractGestureButtonData;
import com.tom.cpm.shared.parts.anim.menu.BoolParameterToggleButtonData;
import com.tom.cpm.shared.parts.anim.menu.ValueParameterButtonData;
import com.tom.cpm.shared.util.Log;

/**
 * Finds this mod's animations inside a loaded model, once per model.
 */
final class ModelBindings {
	private static final Map<ModelDefinition, List<LayerBinding>> CACHE = new WeakHashMap<>();

	private ModelBindings() {
	}

	static List<LayerBinding> of(ModelDefinition def) {
		List<LayerBinding> list = CACHE.get(def);
		if (list == null) {
			list = scan(def);
			CACHE.put(def, list);
		}
		return list;
	}

	private static List<LayerBinding> scan(ModelDefinition def) {
		List<LayerBinding> out = new ArrayList<>();
		try {
			for (AbstractGestureButtonData btn : def.getAnimations().getNamedActions()) {
				String name = btn.getName();
				if (name == null) continue;
				TriggerSettings st = TriggerSettings.parse(name);
				if (st == null) {
					if (TriggerSettings.normalize(name).startsWith(TriggerType.PREFIX)) {
						Log.warn("[Compelium's CPM+] Unknown trigger name in model: '" + name + "'");
					}
					continue;
				}
				if (btn instanceof ValueParameterButtonData) {
					out.add(new LayerBinding(name, st, ((ValueParameterButtonData) btn).parameter, 0));
				} else if (btn instanceof BoolParameterToggleButtonData) {
					BoolParameterToggleButtonData t = (BoolParameterToggleButtonData) btn;
					if (t.mask != 0) out.add(new LayerBinding(name, st, t.parameter, t.mask));
				} else {
					Log.warn("[Compelium's CPM+] '" + name + "' must be a Value layer or a Layer animation to work as a trigger");
				}
			}
		} catch (Throwable e) {
			Log.warn("[Compelium's CPM+] Could not read the model's animations", e);
			return Collections.emptyList();
		}
		if (!out.isEmpty()) {
			Log.info("[Compelium's CPM+] Found " + out.size() + " trigger animation(s) in a model");
		}
		return out.isEmpty() ? Collections.<LayerBinding>emptyList() : Collections.unmodifiableList(out);
	}
}
