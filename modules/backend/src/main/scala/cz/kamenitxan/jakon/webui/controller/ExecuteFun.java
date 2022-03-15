package cz.kamenitxan.jakon.webui.controller;

import spark.route.HttpMethod;

import java.lang.annotation.*;

@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecuteFun {
	String path();

	HttpMethod method();
}
