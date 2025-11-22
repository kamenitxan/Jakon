package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.AbstractPagelet
import io.javalin.rendering.FileRenderer

/**
 * Created by TPa on 2018-11-27.
 */
abstract class AbstractAdminPagelet extends AbstractPagelet {
	override val engine: FileRenderer = Settings.getAdminEngine

	val name: String
	val icon: String = "fa-cog"

}
