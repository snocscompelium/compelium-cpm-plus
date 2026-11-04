/*
 * Compile-only stand-in for SpongePowered Mixin's @Mixin (MIT license).
 * Not packaged in the mod: Fabric Loader provides the real class at runtime.
 * Only the elements this project uses are declared.
 */
package org.spongepowered.asm.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Mixin {
	Class<?>[] value() default {};
	String[] targets() default {};
	int priority() default 1000;
	boolean remap() default true;
}
