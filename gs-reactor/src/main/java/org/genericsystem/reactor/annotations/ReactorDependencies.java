package org.genericsystem.reactor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.genericsystem.reactor.gs.GSTagImpl;

/**
 * @author Nicolas Feybesse
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface ReactorDependencies {
	Class<? extends GSTagImpl>[] value();
}