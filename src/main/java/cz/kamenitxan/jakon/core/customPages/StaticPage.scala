package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.template.TemplateUtils

/**
  * Created by TPa on 13.08.16.
  */
class StaticPage(templateName: String, url: String) extends CustomPage {
	override def render() {
		val engine = TemplateUtils.getEngine
		engine.render(templateName, url, null)
	}
}
