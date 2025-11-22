package cz.kamenitxan.jakon.core.dynamic;

/**
 * Created by TPa on 06.03.2022.
 */
public @interface Delete {
	String path();

	boolean validate() default true;
}
