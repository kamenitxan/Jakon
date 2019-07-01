package cz.kamenitxan.jakon.core.customPages


import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils

/**
  * Created by TPa on 26.04.16.
  */
abstract class AbstractCustomPage extends JakonObject(childClass = classOf[AbstractCustomPage].getName) with IControler {
	protected val engine: TemplateEngine = TemplateUtils.getEngine

}