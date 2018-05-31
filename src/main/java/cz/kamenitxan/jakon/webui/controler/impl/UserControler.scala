package cz.kamenitxan.jakon.webui.controler.impl

import java.lang.reflect.Field

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.controler.impl.ObjectControler.excludedFields
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
  * Created by TPa on 01.05.18.
  */
object UserControler {

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
			val fieldRef: Field = Utils.getFieldsUpTo(user.getClass, classOf[Object]).find(f => f.getName.equals(p)).get
			fieldRef.setAccessible(true)
			val value = req.queryParams(p).conform(fieldRef)
			// TODO: editace ACL
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

		user.update()
		res.redirect("/admin/profile")
		new Context(Map[String, Any](), "pages/profile")
	}
}
