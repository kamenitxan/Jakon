package cz.kamenitxan.jakon.core.template

import java.util.Map

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
trait TemplateEngine {
	def render(templateName: String, path: String, context: java.util.Map[String, AnyRef])
}