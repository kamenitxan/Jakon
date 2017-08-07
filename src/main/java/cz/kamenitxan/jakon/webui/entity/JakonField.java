package cz.kamenitxan.jakon.webui.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JakonField {
	boolean required() default true;
	boolean editable() default true;
	boolean shown() default true;
	String htmlClass() default "";
	String inputTemplate() default "";
}
