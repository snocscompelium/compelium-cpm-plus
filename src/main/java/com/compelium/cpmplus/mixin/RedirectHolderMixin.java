package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.bends.BendApplier;
import com.compelium.cpmplus.bends.BendSources;

/** Hooks into CPM's model posing, which is the same code on every Minecraft version. */
@Pseudo
@Mixin(targets = "com.tom.cpm.shared.model.render.ModelRenderManager$RedirectHolder", remap = false)
public abstract class RedirectHolderMixin {
	/** The main swapIn (its last parameter changed in CPM 0.6.26; sub-models also pass through it). */
	@Inject(method = {
			"swapIn(Lcom/tom/cpm/shared/definition/ModelDefinition;Ljava/lang/Object;Lcom/tom/cpm/shared/config/Player;Lcom/tom/cpm/shared/animation/AnimationState;)V",
			"swapIn(Lcom/tom/cpm/shared/definition/ModelDefinition;Ljava/lang/Object;Lcom/tom/cpm/shared/config/Player;Lcom/tom/cpm/shared/animation/AnimationEngine$AnimationMode;)V"
	}, at = @At("HEAD"), require = 0, remap = false)
	private void cpmplus$drawStart(CallbackInfo ci) {
		BendSources.onDrawStart();
	}

	/** After CPM posed the model and played its own animations, before copy transforms. */
	@Inject(method = "bindFirstSetup(Z)V",
			at = @At(value = "INVOKE", target = "Lcom/tom/cpm/shared/animation/AnimationRegistry;applyCopy()V", remap = false),
			require = 0, remap = false)
	private void cpmplus$applyBends(boolean isPoseSetup, CallbackInfo ci) {
		BendApplier.onPoseReady(this);
	}
}
