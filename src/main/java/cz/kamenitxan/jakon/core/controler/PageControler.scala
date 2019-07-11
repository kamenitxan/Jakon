package cz.kamenitxan.jakon.core.controler

import java.util

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
  */
class PageControler extends IControler {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val template = "page"

	val ALL_PAGES_SQL = "SELECT * FROM Page JOIN JakonObject ON Page.id = JakonObject.id"

	def generate() {
		val e: TemplateEngine = TemplateUtils.getEngine
		val conn = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()
			val pages = DBHelper.select(stmt, ALL_PAGES_SQL, classOf[Page]).map(qr => qr.entity.asInstanceOf[Page])
			pages.foreach(p => {
				val context = new util.HashMap[String, AnyRef]
				context.put("page", p)
				e.render(template, p.url, context)
			})
		} catch {
			case ex: Exception => logger.error("Exception occurred while generation of Pages", ex)
		} finally {
			conn.close()
		}
	}
}