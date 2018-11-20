package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 13.08.16.
  */
abstract class AbstractStaticPage(templateName: String, url: String) extends AbstractCustomPage {
	super.setUrl(url)



	override def generate() {
		engine.render(templateName, url, Map[String, AnyRef]())
	}

	override val objectSettings: ObjectSettings = new ObjectSettings()
}
