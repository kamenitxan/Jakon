package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a number whose value must be lower or
 * equal to the specified maximum.
 *
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(DummyValidator.class)
public @interface DecimalMax {
	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * The {@code String} representation of the max value according to the
	 * {@code BigDecimal} string representation.
	 *
	 * @return value the element must be lower or equal to
	 */
	String value();

	/**
	 * Specifies whether the specified maximum is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the value must be lower or equal to the specified maximum,
	 * {@code false} if the value must be lower
	 */
	boolean inclusive() default true;

}
