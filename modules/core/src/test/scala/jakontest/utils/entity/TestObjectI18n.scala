package jakontest.utils.entity

import cz.kamenitxan.jakon.core.database.{DBHelper, JakonField}
import cz.kamenitxan.jakon.core.model.I18nData

/**
  * Created by TPa on 06.12.2020.
  */
class TestObjectI18n extends I18nData {
	@JakonField
	var name: String = _

	@JakonField
	var description: String = _

	def create(): Int = {
		DBHelper.withDbConnection(implicit conn => {
			val sql = s"INSERT INTO $className (id, locale) VALUES (?, ?)"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, id)
			stmt.setString(2, locale.toString)
			stmt.executeUpdate()
		})
		-1
	}

	override def update(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			//language=SQL
			val sql = s"UPDATE $className SET name = ?, description = ? WHERE id = ? AND locale = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setString(1, name)
			stmt.setString(2, description)
			stmt.setInt(3, id)
			stmt.setString(4, locale.toString)
			stmt.executeUpdate()
		})

	}
}
