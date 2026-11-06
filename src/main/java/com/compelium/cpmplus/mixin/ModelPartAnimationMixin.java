package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tom.cpm.shared.definition.ModelDefinition;

import com.compelium.cpmplus.bends.LegacyBendReader;

/** CPM's default animation format: look for the bend marker animations before they are registered. */
@Pseudo
@Mixin(targets = "com.tom.cpm.shared.parts.ModelPartAnimation", remap = false)
public abstract class ModelPartAnimationMixin {
	@Inject(method = "apply(Lcom/tom/cpm/shared/definition/ModelDefinition;)V", at = @At("HEAD"), require = 0, remap = false)
	private void cpmplus$findBendsLegacy(ModelDefinition def, CallbackInfo ci) {
		LegacyBendReader.onApply(this, def);
	}
}
