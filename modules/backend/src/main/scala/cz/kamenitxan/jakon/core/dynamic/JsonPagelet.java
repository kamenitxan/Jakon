package cz.kamenitxan.jakon.core.dynamic;

import java.lang.annotation.*;

/**
 * Created by TPa on 13.04.2020.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface JsonPagelet {
	String path() default "/";
}
