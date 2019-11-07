package cz.kamenitxan.jakon.webui.controler.objectextension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectExtension {
	Class value();

	ExtensionType extensionType();

}
