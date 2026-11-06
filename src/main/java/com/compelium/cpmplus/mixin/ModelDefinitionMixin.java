package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.effects.RenderCapture;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemRenderer;

/**
 * CPM calls storeTransform for every item holder part it draws, with that part's position.
 * The particle markers are item holders, so this is where their positions come from.
 */
@Mixin(value = ModelDefinition.class, remap = false)
public abstract class ModelDefinitionMixin {

	@Inject(method = "storeTransform", at = @At("HEAD"), remap = false, require = 0)
	private void cpmplus$captureMarker(ItemRenderer render, MatrixStack stack, boolean doRender, CallbackInfo ci) {
		RenderCapture.onStoreTransform((ModelDefinition) (Object) this, render, stack, doRender);
	}
}
