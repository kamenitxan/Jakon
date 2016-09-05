package cz.kamenitxan.jakon.core.template

import java.util

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
trait TemplateEngine {
	def render(templateName: String, path: String, context: util.Map[String, AnyRef])
}