package com.compelium.cpmplus.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.effects.RenderCapture;
import com.compelium.cpmplus.effects.ShaderCompat;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

/**
 * Marks the stretch where Minecraft draws an entity in the world, and remembers the
 * camera position and the view of this drawing, so particles only come from players drawn
 * in the world (never from the inventory preview, the editor or a shader's shadow pass).
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "renderEntity", at = @At("HEAD"))
	private void cpmplus$enterEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
		float[] view = null;
		if (poseStack != null) {
			view = new float[16];
			poseStack.last().pose().get(view);
		}
		RenderCapture.enterWorld(cameraX, cameraY, cameraZ, view, ShaderCompat.isShadowPass());
	}

	@Inject(method = "renderEntity", at = @At("RETURN"))
	private void cpmplus$exitEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
		RenderCapture.exitWorld();
	}
}
