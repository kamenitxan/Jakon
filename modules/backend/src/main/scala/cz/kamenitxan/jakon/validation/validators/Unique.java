package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be unique.
 * <p>
 * Supported types are:
 * <ul>
 *     <li>{@code CharSequence}</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(UniqueValidator.class)
public @interface Unique {

	MessageSeverity severity() default MessageSeverity.ERROR;

}
