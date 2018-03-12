package cz.kamenitxan.jakon.webui.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JakonField {
	boolean required() default true;
	boolean disabled() default false;
	boolean shown() default true;
	String htmlClass() default "";
	int htmlMaxLength() default 255;
	String inputTemplate() default "";
}
