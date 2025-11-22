package cz.kamenitxan.jakon.core.controller

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.logging.Logger

import java.sql.Connection
import java.util

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
  */
class PageController extends IController {

	val template = "page"

	val ALL_PAGES_SQL = "SELECT * FROM Page JOIN JakonObject ON Page.id = JakonObject.id"

	def generate(): Unit = {
		val e: TemplateEngine = TemplateUtils.getEngine
		implicit val conn: Connection = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()
			val pages = DBHelper.select(stmt, ALL_PAGES_SQL, classOf[Page]).map(qr => qr.entity)
			pages.foreach(p => {
				e.render(template, p.url, Map(
					"page" -> p
				))
			})
		} catch {
			case ex: Exception => Logger.error("Exception occurred while generation of Pages", ex)
		} finally {
			conn.close()
		}
	}
}