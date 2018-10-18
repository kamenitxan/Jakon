package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.template.TemplateUtils
import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 13.08.16.
  */
abstract class StaticPage(templateName: String, url: String) extends CustomPage {
	super.setUrl(url)



	override def generate() {
		val engine = TemplateUtils.getEngine
		engine.render(templateName, url, Map[String, AnyRef]())
	}

	override val objectSettings: ObjectSettings = new ObjectSettings()
}
