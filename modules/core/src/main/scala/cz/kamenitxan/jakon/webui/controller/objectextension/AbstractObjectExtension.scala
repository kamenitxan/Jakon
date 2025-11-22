package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet

/**
 * Extension of object in administration. Extension template should be in templates/admin/objects/extension/NameOfExtensionClass.peb
 * */
abstract class AbstractObjectExtension extends AbstractAdminPagelet {

	override val name: String = this.getClass.getName
}
