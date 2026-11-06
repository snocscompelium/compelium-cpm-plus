package com.compelium.cpmplus.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.effects.RenderCapture;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

/**
 * Marks the stretch where Minecraft draws an entity in the world, and remembers the
 * camera position, so particles only come from players drawn in the world (never from
 * the inventory preview or the editor).
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "renderEntity", at = @At("HEAD"))
	private void cpmplus$enterEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
		RenderCapture.enterWorld(cameraX, cameraY, cameraZ);
	}

	@Inject(method = "renderEntity", at = @At("RETURN"))
	private void cpmplus$exitEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
		RenderCapture.exitWorld();
	}
}
