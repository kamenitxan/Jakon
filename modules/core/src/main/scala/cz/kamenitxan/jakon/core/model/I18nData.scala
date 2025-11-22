package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.database.converters.LocaleConverter
import cz.kamenitxan.jakon.core.database.{DBHelper, JakonField}

import java.util.Locale

/**
 * Created by TPa on 06.12.2020.
 */
abstract class I18nData(implicit s: sourcecode.FullName) extends BaseEntity {
	var id: Integer = _
	@JakonField(converter = classOf[LocaleConverter], inputTemplate = "String")
	var locale: Locale = _
	val className: String = s.value.substring(s.value.lastIndexOf(".") + 1)

	def create(): Int

	def update(): Unit

	def delete(): Unit = {
		val sql = s"DELETE FROM $className WHERE id = ? AND locale = ?"
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, id)
			stmt.setString(2, locale.toString)
			stmt.executeUpdate()
		})
	}
}
