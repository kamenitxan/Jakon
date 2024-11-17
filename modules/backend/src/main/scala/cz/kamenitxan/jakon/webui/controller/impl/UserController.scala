package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.ModelAndView
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer.*
import io.javalin.http.Context

import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

/**
  * Created by TPa on 01.05.18.
  */
object UserController {

	private val excludedFields = ObjectController.excludedFields ++ Seq("acl")

	def render(ctx: Context): ModelAndView = {
		if (PageContext.getInstance().getLoggedUser.isEmpty) {
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
				"objectName" -> classOf[JakonUser].getSimpleName
			), "pages/unauthorized")
		}
		val user = PageContext.getInstance().getLoggedUser.get
		val fields = Utils.getFieldsUpTo(user.getClass, classOf[Object]).filter(n => !excludedFields.contains(n.getName))
		val f = FieldConformer.getFieldInfos(user, fields).asJava
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
			"objectName" -> classOf[JakonUser].getSimpleName,
			"object" -> user,
			"id" -> user.id,
			"fields" -> f), "pages/profile")
	}

	def update(ctx: Context): ModelAndView = {
		if (PageContext.getInstance().getLoggedUser.isEmpty) {
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
				"objectName" -> classOf[JakonUser].getSimpleName
			), "pages/unauthorized")
		}
		val params = ctx.queryParamMap().asScala
		val user = PageContext.getInstance().getLoggedUser.get

		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRefOpt = Utils.getFieldsUpTo(user.getClass, classOf[Object]).find(f => f.getName.equals(p))
			if (fieldRefOpt.isDefined) {
				val fieldRef = fieldRefOpt.get
				fieldRef.setAccessible(true)
				val pad = p
				// val value = ctx.queryParam(p).conform(fieldRef) // TODO JAVALIN FIX
				val value = "adsasda"
				if (value != null) {
					if (p.equals("password")) {
						if (!value.asInstanceOf[String].startsWith("$2a$")) {
							fieldRef.set(user, value)
						}
					} else {
						fieldRef.set(user, value)
					}
				}
			}
		}

		user.update()
		ctx.redirect("/admin/profile")
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), "pages/profile")
	}
}
