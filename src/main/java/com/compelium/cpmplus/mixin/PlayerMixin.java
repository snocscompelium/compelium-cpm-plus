package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.physics.PhysicsEngine;
import com.tom.cpm.shared.animation.AnimationState;
import com.tom.cpm.shared.config.Player;

/**
 * Hooks into CPM's own (Minecraft-version independent) player code, right after
 * CPM has read the player's movement for this frame.
 */
@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin {

	@Inject(method = "updatePlayer", at = @At("TAIL"), remap = false, require = 0)
	private void cpmplus$applyTriggers(Object player, AnimationState state, CallbackInfo ci) {
		PhysicsEngine.apply((Player<?>) (Object) this, state);
	}
}
