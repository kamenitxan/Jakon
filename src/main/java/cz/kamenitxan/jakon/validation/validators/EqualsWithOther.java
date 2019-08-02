package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Compares annotated element with other field on validated object
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(DummyValidator.class)
public @interface EqualsWithOther {

	MessageSeverity severity() default MessageSeverity.ERROR;

	/**
	 * @return name of other field
	 */
	String value();
}
