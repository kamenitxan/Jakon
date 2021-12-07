package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet

abstract class AbstractObjectExtension extends AbstractAdminPagelet {

	override val name: String = this.getClass.getName
}
