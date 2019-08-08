package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element size must be between the specified boundaries (included).
 * <p>
 * Supported types are:
 * <ul>
 *     <li>{@code CharSequence} (length of character sequence is evaluated)</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(SizeValidator.class)
public @interface Size {

	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * @return size the element must be higher or equal to
	 */
	int min() default 0;

	/**
	 * @return size the element must be lower or equal to
	 */
	int max() default Integer.MAX_VALUE;


}
