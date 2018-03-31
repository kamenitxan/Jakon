package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.template.TemplateUtils
import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 13.08.16.
  */
class StaticPage(templateName: String, url: String) extends CustomPage {
	override def render() {
		val engine = TemplateUtils.getEngine
		engine.render(templateName, url, null)
	}

	override val objectSettings: ObjectSettings = new ObjectSettings()
}
