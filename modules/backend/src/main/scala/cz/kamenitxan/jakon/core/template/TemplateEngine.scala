package cz.kamenitxan.jakon.core.template

import java.util

import cz.kamenitxan.jakon.core.controller.IController

import scala.jdk.CollectionConverters._

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
trait TemplateEngine {
	def render(templateName: String, path: String, context: util.Map[String, AnyRef])(implicit caller: IController): Unit

	def render(templateName: String, path: String, context: Map[String, AnyRef])(implicit caller: IController): Unit = {
		render(templateName, path, context.asJava)
	}

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