package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.function.FunctionHelper
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}
import java.util.Date
import java.util.regex.Pattern

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Post extends JakonObject {
	@JakonField(listOrder = 4)
	var date: Date = _
	@JakonField(required = false, listOrder = 2)
	var perex: String = _
	@ManyToOne
	@JakonField(required = false, inputTemplate = "String", listOrder = 5)
	var category: Category = _
	@JakonField(listOrder = 1, searched = true)
	var title: String = _
	@JakonField(inputTemplate = "textarea", listOrder = 3)
	var content: String = ""
	@JakonField(listOrder = 6)
	var showComments: Boolean = false


	def getContent: String = {
		if (content == null) return ""
		val parsedHtml = TemplateUtils.parseMarkdown(content)
		// TODO: parsovani funkci
		// (\{)((?:[a-z][a-z]+)).*?(\})
		val p = Pattern.compile("\\{((?:\\w+))\\((.*?)\\)}")
		val m = p.matcher(parsedHtml)
		val result = new StringBuffer
		while ( {
			m.find
		}) {
			val funcion = m.group(1)
			val params = m.group(2)
			m.appendReplacement(result, FunctionHelper.getFunction(funcion).execute(FunctionHelper.splitParams(params)))
		}
		m.appendTail(result)
		result.toString
	}


	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO Post (id, date, perex, category_id, title, content, showComments) VALUES (?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		if (date != null) {
			stmt.setDate(2, new java.sql.Date(date.getTime))
		} else {
			stmt.setNull(2, Types.DATE)
		}
		stmt.setString(3, perex)
		if (category != null) {
			stmt.setInt(4, category.id)
		} else {
			stmt.setNull(4, Types.INTEGER)
		}
		stmt.setString(5, title)
		stmt.setString(6, content)
		stmt.setBoolean(7, showComments)

		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE Post SET date = ?, perex = ?, category_id = ?, title = ?, content = ?, showComments = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		if (date != null) {
			stmt.setDate(1, new java.sql.Date(date.getTime))
		} else {
			stmt.setNull(1, Types.DATE)
		}
		stmt.setString(2, perex)
		if (category != null) {
			stmt.setInt(3, category.id)
		} else {
			stmt.setNull(3, Types.INTEGER)
		}
		stmt.setString(4, title)
		stmt.setString(5, content)
		stmt.setBoolean(6, showComments)
		stmt.setInt(7, jid)
		stmt.executeUpdate()
	}

	override val objectSettings: ObjectSettings = null
}