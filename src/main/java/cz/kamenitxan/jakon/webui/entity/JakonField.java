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

	/** shown in admin edit view */
	boolean shownInEdit() default true;
	/** shown in admin list view */
	boolean shownInList() default true;
	/** order of column in list and edit view */
	int listOrder() default 999;
	String htmlClass() default "";
	int htmlMaxLength() default 255;
	String inputTemplate() default "";

	/**
	 * property is searchable in admin, including in select search box
	 */
	boolean searched() default false;
}
