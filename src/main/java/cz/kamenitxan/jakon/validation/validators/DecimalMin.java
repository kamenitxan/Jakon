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
 *
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(DummyValidator.class)
public @interface DecimalMin {
	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * The {@code String} representation of the min value according to the
	 * {@code BigDecimal} string representation.
	 *
	 * @return value the element must be higher or equal to
	 */
	String value();

	/**
	 * Specifies whether the specified minimum is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the value must be higher or equal to the specified minimum,
	 * {@code false} if the value must be higher
	 */
	boolean inclusive() default true;

}
