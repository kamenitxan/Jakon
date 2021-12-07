package cz.kamenitxan.jakon.core.custom_pages

/**
  * Created by TPa on 13.08.16.
  */
abstract class AbstractStaticPage(val templateName: String, pageUrl: String) extends AbstractCustomPage {

	override def generate(): Unit = {
		engine.render(templateName, pageUrl, Map[String, AnyRef]())
	}

}
