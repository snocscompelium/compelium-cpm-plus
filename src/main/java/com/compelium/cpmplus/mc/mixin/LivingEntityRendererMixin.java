package com.compelium.cpmplus.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.effects.RenderCapture;
import com.compelium.cpmplus.mc.McParticleSink;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Gives the particle code the pose of the model's root right before the model is drawn
 * (CPM reports marker positions relative to it), and spawns the particles once the
 * entity is fully drawn.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	private static final String RENDER = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void cpmplus$beginEntity(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
		RenderCapture.beginEntity();
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
	private void cpmplus$modelRoot(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
		float[] m = new float[16];
		poseStack.last().pose().get(m);
		RenderCapture.modelRoot(m);
	}

	@Inject(method = RENDER, at = @At("RETURN"))
	private void cpmplus$endEntity(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
		RenderCapture.endEntity(McParticleSink.INSTANCE);
	}
}
