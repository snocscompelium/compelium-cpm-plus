/*
 * Compile-only stand-in for SpongePowered Mixin's @At (MIT license).
 * Not packaged in the mod: Fabric Loader provides the real class at runtime.
 */
package org.spongepowered.asm.mixin.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface At {
	String id() default "";
	String value();
	String target() default "";
	int ordinal() default -1;
	boolean remap() default true;
}
