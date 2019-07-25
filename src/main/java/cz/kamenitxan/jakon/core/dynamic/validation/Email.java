package cz.kamenitxan.jakon.core.dynamic.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = EmailValidatorImpl.class)
@Documented
public @interface Email {
	String message() default "{emailValidator.error}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
