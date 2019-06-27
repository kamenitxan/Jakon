package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 13.08.16.
  */
abstract class AbstractStaticPage(val templateName: String, pageUrl: String) extends AbstractCustomPage {
	this.url = pageUrl


	override def generate() {
		engine.render(templateName, url, Map[String, AnyRef]())
	}

	override val objectSettings: ObjectSettings = new ObjectSettings()
}
