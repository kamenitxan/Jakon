package cz.kamenitxan.jakon.core.template

import cz.kamenitxan.jakon.core.controller.IController

import java.util
import scala.jdk.CollectionConverters.*

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
trait TemplateEngine {

	def render(templateName: String, path: String, context: Map[String, AnyRef])(implicit caller: IController): Unit

	/**
	  * Renders template with provided name
	  *
	  * @param templateName template name
	  * @param context      render parameters
	  * @return rendered template
	  */
	def renderToString(templateName: String, context: Map[String, AnyRef]): String

	/**
	  * Renders provided template
	  *
	  * @param template template to render
	  * @param context  render parameters
	  * @return rendered template
	  */
	def renderTemplate(template: String, context: Map[String, AnyRef]): String
}