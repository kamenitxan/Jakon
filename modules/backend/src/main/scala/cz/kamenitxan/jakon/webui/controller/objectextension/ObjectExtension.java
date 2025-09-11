package cz.kamenitxan.jakon.webui.controller.objectextension;

import cz.kamenitxan.jakon.core.model.JakonObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define an extension for a specific object type in the administration.
 * <p>
 * Attributes:
 * - `value`: The class of the object (subclass of JakonObject) for which this extension is defined.
 * - `extensionType`: The type of extension to be applied, determining the context of usage
 *                    (e.g., in a list view, single object view, or both).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectExtension {
	Class<? extends JakonObject> value();

	ExtensionType extensionType();

}
