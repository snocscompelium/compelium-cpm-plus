package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.parts.anim.AnimLoaderState;

import com.compelium.cpmplus.bends.BendRigBuilder;
import com.compelium.cpmplus.bends.Bends;
import com.compelium.cpmplus.bends.RigHolder;

/** CPM's newer animation format ("experimental export"): look for the bend marker animations. */
@Pseudo
@Mixin(targets = "com.tom.cpm.shared.parts.anim.ModelPartAnimationNew", remap = false)
public abstract class ModelPartAnimationNewMixin {
	@Shadow(remap = false)
	private AnimLoaderState state;

	@Inject(method = "apply(Lcom/tom/cpm/shared/definition/ModelDefinition;)V", at = @At("HEAD"), require = 0, remap = false)
	private void cpmplus$findBends(ModelDefinition def, CallbackInfo ci) {
		try {
			if (state == null || !(def instanceof RigHolder) || !Bends.enabled()) return;
			BendRigBuilder.build(state, def);
		} catch (Throwable t) {
			Bends.warn("Could not read the emote bends of this model.", t);
		}
	}
}
