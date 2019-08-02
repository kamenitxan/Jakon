package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The string has to be a well-formed email address. Exact semantics of what makes up a valid
 * email address are left to Bean Validation providers. Accepts {@code String}.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(EmailValidator.class)
public @interface Email {
	MessageSeverity severity() default MessageSeverity.ERROR;
}
