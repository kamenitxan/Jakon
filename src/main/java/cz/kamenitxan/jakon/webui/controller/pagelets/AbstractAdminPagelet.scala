package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.AbstractPagelet
import spark.TemplateEngine

/**
 * Created by TPa on 2018-11-27.
 */
abstract class AbstractAdminPagelet extends AbstractPagelet {
	override val engine: TemplateEngine = Settings.getAdminEngine

	val name: String
	val icon: String = "fa-cog"

}
