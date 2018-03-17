package cz.kamenitxan.jakon.webui.controler;

import spark.route.HttpMethod;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecuteFun {
	String path();
	HttpMethod method();
}
