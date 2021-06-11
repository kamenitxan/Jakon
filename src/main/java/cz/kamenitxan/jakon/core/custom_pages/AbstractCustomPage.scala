package cz.kamenitxan.jakon.core.custom_pages


import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils

/**
  * Created by TPa on 26.04.16.
  */
abstract class AbstractCustomPage extends IController {
	protected val engine: TemplateEngine = TemplateUtils.getEngine

}