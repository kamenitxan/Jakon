package cz.kamenitxan.jakon.webui.controller.objectextension;

import cz.kamenitxan.jakon.core.model.JakonObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectExtension {
	Class<? extends JakonObject> value();

	ExtensionType extensionType();

}
