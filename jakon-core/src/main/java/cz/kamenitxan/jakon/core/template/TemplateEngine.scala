package cz.kamenitxan.jakon.core.template

import java.util

import cz.kamenitxan.jakon.core.controler.IControler

import scala.collection.JavaConverters._

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
trait TemplateEngine {
	def render(templateName: String, path: String, context: util.Map[String, AnyRef])(implicit caller: IControler): Unit

	def render(templateName: String, path: String, context: Map[String, AnyRef])(implicit caller: IControler): Unit = {
		render(templateName, path, mapAsJavaMap(context))
	}

	/**
	  * Renders template with provided name
	  * @param templateName template name
	  * @param context render parameters
	  * @return rendered template
	  */
	def renderString(templateName: String, context: Map[String, AnyRef]): String

	/**
	  * Renders provided template
	  * @param template template to render
	  * @param context render parameters
	  * @return rendered template
	  */
	def renderTemplate(template: String, context: Map[String, AnyRef]): String
}