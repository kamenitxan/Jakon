package cz.kamenitxan.jakon.webui.controller.objectextension;

/**
 * Represents the type of extension applied to an object in the administration interface.
 * <p>
 * This enumeration is used to define the context in which an extension is applicable:
 * - LIST: The extension is applicable in a list view.
 * - SINGLE: The extension is applicable in a single object view.
 * - BOTH: The extension is applicable in both list and single views.
 */
public enum ExtensionType {
	LIST,
	SINGLE,
	BOTH
}
