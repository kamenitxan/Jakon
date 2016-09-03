package cz.kamenitxan.jakon.core.controler

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.core.template.TemplateUtils
import java.sql.SQLException
import java.util
import scala.collection.JavaConversions._

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
  */
class PageControler extends IControler {
	val template = "page"

	def generate() {
		val e: TemplateEngine = TemplateUtils.getEngine
		try {
			val pages = DBHelper.getPageDao.queryForAll.toList
			pages.foreach(p => {
				val context = new util.HashMap[String, AnyRef]
				context.put("page", p)
				e.render(template, p.getUrl, context)
			})
		} catch {
			case ex: SQLException => ex.printStackTrace()
		}
	}
}