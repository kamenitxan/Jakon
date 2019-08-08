/*
 * Bean Validation API
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a number whose value must be higher or
 * equal to the specified minimum.
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(MinValidator.class)
public @interface Min {

	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * @return value the element must be higher or equal to
	 */
	long value();

}
