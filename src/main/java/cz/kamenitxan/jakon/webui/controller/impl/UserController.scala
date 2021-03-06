package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import spark.{ModelAndView, Request, Response}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
  * Created by TPa on 01.05.18.
  */
object UserController {

	private val excludedFields = ObjectController.excludedFields ++ Seq("acl")

	def render(req: Request, res: Response): ModelAndView = {
		if (PageContext.getInstance().getLoggedUser.isEmpty) {
			return new Context(Map[String, Any](
				"objectName" -> classOf[JakonUser].getSimpleName
			), "pages/unauthorized")
		}
		val user = PageContext.getInstance().getLoggedUser.get
		val fields = Utils.getFieldsUpTo(user.getClass, classOf[Object]).filter(n => !excludedFields.contains(n.getName))
		val f = FieldConformer.getFieldInfos(user, fields).asJava
		new Context(Map[String, Any](
			"objectName" -> classOf[JakonUser].getSimpleName,
			"object" -> user,
			"id" -> user.id,
			"fields" -> f), "pages/profile")
	}

	def update(req: Request, res: Response): ModelAndView = {
		if (PageContext.getInstance().getLoggedUser.isEmpty) {
			return new Context(Map[String, Any](
				"objectName" -> classOf[JakonUser].getSimpleName
			), "pages/unauthorized")
		}
		val params = req.queryParams() asScala
		val user = PageContext.getInstance().getLoggedUser.get

		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRefOpt = Utils.getFieldsUpTo(user.getClass, classOf[Object]).find(f => f.getName.equals(p))
			if (fieldRefOpt.isDefined) {
				val fieldRef = fieldRefOpt.get
				fieldRef.setAccessible(true)
				val value = req.queryParams(p).conform(fieldRef)
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
		res.redirect("/admin/profile")
		new Context(Map[String, Any](), "pages/profile")
	}
}
