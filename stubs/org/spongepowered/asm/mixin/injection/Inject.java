/*
 * Compile-only stand-in for SpongePowered Mixin's @Inject (MIT license).
 * Not packaged in the mod: Fabric Loader provides the real class at runtime.
 */
package org.spongepowered.asm.mixin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
	String id() default "";
	String[] method() default {};
	At[] at();
	boolean cancellable() default false;
	boolean remap() default true;
	int require() default -1;
	int expect() default 1;
	int allow() default -1;
}
