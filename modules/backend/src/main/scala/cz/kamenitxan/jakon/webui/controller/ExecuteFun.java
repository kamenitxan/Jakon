package cz.kamenitxan.jakon.webui.controller;

import java.lang.annotation.*;
import cz.kamenitxan.jakon.webui.HttpMethod;

@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecuteFun {
	String path();

	HttpMethod method();
}
