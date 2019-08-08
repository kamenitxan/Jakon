package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a number within accepted range
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(DummyValidator.class)
public @interface Digits {
	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * @return maximum number of integral digits accepted for this number
	 */
	int integer();

	/**
	 * @return maximum number of fractional digits accepted for this number
	 */
	int fraction();

}
