/*
 * Compile-only stand-in for SpongePowered Mixin's CallbackInfo (MIT license).
 * Not packaged in the mod: Fabric Loader provides the real class at runtime.
 */
package org.spongepowered.asm.mixin.injection.callback;

public class CallbackInfo {
	public String getId() {
		return null;
	}

	public boolean isCancellable() {
		return false;
	}

	public boolean isCancelled() {
		return false;
	}

	public void cancel() {
	}
}
