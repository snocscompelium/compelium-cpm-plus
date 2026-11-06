package com.compelium.cpmplus.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.tom.cpm.shared.animation.AnimationRegistry;
import com.tom.cpm.shared.animation.AnimationTrigger;
import com.tom.cpm.shared.animation.IAnimation;

import com.compelium.cpmplus.bends.TriggerMarker;

/** Bend marker animations only tag pieces; they are never registered to play in game. */
@Pseudo
@Mixin(targets = "com.tom.cpm.shared.parts.anim.SerializedTrigger", remap = false)
public abstract class SerializedTriggerMixin implements TriggerMarker {
	@Unique
	private boolean cpmplus$marker;

	@Override
	public boolean cpmplus$isMarker() {
		return cpmplus$marker;
	}

	@Override
	public void cpmplus$setMarker(boolean marker) {
		cpmplus$marker = marker;
	}

	@Inject(method = "compile(Lcom/tom/cpm/shared/animation/AnimationRegistry;Ljava/util/List;)Lcom/tom/cpm/shared/animation/AnimationTrigger;",
			at = @At("RETURN"), cancellable = true, require = 0, remap = false)
	private void cpmplus$skipMarker(AnimationRegistry reg, List<IAnimation> animations, CallbackInfoReturnable<AnimationTrigger> cir) {
		if (cpmplus$marker) cir.setReturnValue(null);
	}
}
