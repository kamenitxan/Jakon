package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.core.database.{DBHelper, I18n, JakonField}
import cz.kamenitxan.jakon.core.model.I18nData
import cz.kamenitxan.jakon.shop.entity.ShopCategory
import cz.kamenitxan.jakon.utils.SqlGen

import java.sql.Connection

/** Category with translations only, its table has no own columns. */
class TestShopCategory extends ShopCategory {

	@JakonField
	@I18n(genericClass = classOf[TestShopCategoryI18n])
	var i18n: Seq[TestShopCategoryI18n] = scala.compiletime.uninitialized

	override def createObject(jid: Int, conn: Connection): Int = {
		super.createObject(jid, conn)
		executeInsert(SqlGen.insertStmt(this, conn, jid, false))
	}
}

class TestShopCategoryI18n extends I18nData {

	@JakonField
	var printTime: String = scala.compiletime.uninitialized

	override def create(): Int = {
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(s"INSERT INTO $className (id, locale, printTime) VALUES (?, ?, ?)")
			stmt.setInt(1, id)
			stmt.setString(2, locale.toString)
			stmt.setString(3, printTime)
			stmt.executeUpdate()
		})
		-1
	}

	override def update(): Unit = {
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(s"UPDATE $className SET printTime = ? WHERE id = ? AND locale = ?")
			stmt.setString(1, printTime)
			stmt.setInt(2, id)
			stmt.setString(3, locale.toString)
			stmt.executeUpdate()
		})
	}
}
