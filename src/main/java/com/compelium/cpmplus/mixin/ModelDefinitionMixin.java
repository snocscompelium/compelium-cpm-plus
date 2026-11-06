package com.compelium.cpmplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compelium.cpmplus.bends.BendRig;
import com.compelium.cpmplus.bends.RigHolder;
import com.compelium.cpmplus.effects.RenderCapture;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.render.ItemRenderer;

/**
 * Two jobs on CPM's loaded model:
 * <ul>
 * <li>CPM calls storeTransform for every item holder part it draws, with that part's position.
 * The particle markers are item holders, so this is where their positions come from.</li>
 * <li>Each model keeps the pieces that follow emote bends (found while the model loads).</li>
 * </ul>
 */
@Mixin(value = ModelDefinition.class, remap = false)
public abstract class ModelDefinitionMixin implements RigHolder {
	@Unique
	private volatile BendRig cpmplus$rig;

	@Unique
	private volatile int cpmplus$flags;

	@Inject(method = "storeTransform", at = @At("HEAD"), remap = false, require = 0)
	private void cpmplus$captureMarker(ItemRenderer render, MatrixStack stack, boolean doRender, CallbackInfo ci) {
		RenderCapture.onStoreTransform((ModelDefinition) (Object) this, render, stack, doRender);
	}

	@Override
	public BendRig cpmplus$getRig() {
		return cpmplus$rig;
	}

	@Override
	public void cpmplus$setRig(BendRig rig) {
		cpmplus$rig = rig;
	}

	@Override
	public int cpmplus$getFlags() {
		return cpmplus$flags;
	}

	@Override
	public void cpmplus$setFlags(int flags) {
		cpmplus$flags = flags;
	}
}
