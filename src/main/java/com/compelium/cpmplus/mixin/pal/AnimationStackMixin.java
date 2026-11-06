package com.compelium.cpmplus.mixin.pal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zigythebird.playeranimcore.bones.PlayerAnimBone;

import com.compelium.cpmplus.bends.BendSources;

/**
 * Player Animation Library (Emotecraft 3.x): copy each body bone's bend as it is computed.
 * Skipped automatically when PAL is not installed.
 */
@Pseudo
@Mixin(targets = "com.zigythebird.playeranimcore.animation.layered.AnimationStack", remap = false)
public abstract class AnimationStackMixin {
	/** PAL 1.0 / 1.1 (Minecraft 1.21.7 - 1.21.11). */
	@Inject(method = "get3DTransform(Lcom/zigythebird/playeranimcore/bones/PlayerAnimBone;)Lcom/zigythebird/playeranimcore/bones/PlayerAnimBone;",
			at = @At("RETURN"), require = 0, remap = false)
	private void cpmplus$boneReturned(PlayerAnimBone bone, CallbackInfoReturnable<PlayerAnimBone> cir) {
		cpmplus$read(cir.getReturnValue() != null ? cir.getReturnValue() : bone);
	}

	/** PAL 1.2+ (Minecraft 26.x). */
	@Inject(method = "get3DTransform(Lcom/zigythebird/playeranimcore/bones/PlayerAnimBone;)V",
			at = @At("RETURN"), require = 0, remap = false)
	private void cpmplus$boneUpdated(PlayerAnimBone bone, CallbackInfo ci) {
		cpmplus$read(bone);
	}

	@Unique
	private static void cpmplus$read(PlayerAnimBone bone) {
		if (bone == null || BendSources.isPalBroken()) return;
		try {
			BendSources.onPalBone(bone.getName(), bone.bend);
		} catch (Throwable t) {
			BendSources.onPalBroken(t);
		}
	}
}
