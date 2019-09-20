package cz.kamenitxan.jakon.webui.controler.objectextension

import cz.kamenitxan.jakon.webui.controler.pagelets.AbstractAdminPagelet

abstract class AbstractObjectExtension extends AbstractAdminPagelet {

	override val name: String = this.getClass.getName
}
