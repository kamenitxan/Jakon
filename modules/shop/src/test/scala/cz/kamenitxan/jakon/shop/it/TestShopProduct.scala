package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.core.database.{DBHelper, I18n, JakonField}
import cz.kamenitxan.jakon.core.model.I18nData
import cz.kamenitxan.jakon.shop.entity.ShopProduct
import cz.kamenitxan.jakon.utils.SqlGen

import java.sql.Connection

/** Application specific product with an own column and translations, stored in its own table joined by id. */
class TestShopProduct extends ShopProduct {

	@JakonField(required = false)
	var subtitle: String = ""

	@JakonField
	@I18n(classOf[TestShopProductI18n])
	var i18n: Seq[TestShopProductI18n] = scala.compiletime.uninitialized

	override def createObject(jid: Int, conn: Connection): Int = {
		super.createObject(jid, conn)
		executeInsert(SqlGen.insertStmt(this, conn, jid, false))
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		super.updateObject(jid, conn)
		SqlGen.updateStmt(this, conn, jid).executeUpdate()
	}
}

class TestShopProductI18n extends I18nData {

	@JakonField
	var description: String = scala.compiletime.uninitialized

	override def create(): Int = {
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(s"INSERT INTO $className (id, locale, description) VALUES (?, ?, ?)")
			stmt.setInt(1, id)
			stmt.setString(2, locale.toString)
			stmt.setString(3, description)
			stmt.executeUpdate()
		})
		-1
	}

	override def update(): Unit = {
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(s"UPDATE $className SET description = ? WHERE id = ? AND locale = ?")
			stmt.setString(1, description)
			stmt.setInt(2, id)
			stmt.setString(3, locale.toString)
			stmt.executeUpdate()
		})
	}
}
