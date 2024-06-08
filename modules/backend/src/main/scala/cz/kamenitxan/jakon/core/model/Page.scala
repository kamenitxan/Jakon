package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.{ManyToOne, Transient}
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Page extends JakonObject with Ordered {
	@JakonField
	var title: String = ""
	@JakonField(inputTemplate = "textarea")
	var content: String = ""
	@ManyToOne
	@JakonField(inputTemplate = "String")
	var parent: Page = _
	@JakonField
	var showComments: Boolean = false
	@JakonField
	var fiel: JakonFile = _
	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	var objectOrder: Double = _
	@Transient
	@JakonField(listOrder = -96)
	var visibleOrder: Int = _

	override def createUrl: String = "/page/" + title.replaceAll(" ", "_").toLowerCase


	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO Page (id, title, parent_id, showComments, objectOrder, content) VALUES (?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, title)
		if (parent == null) {
			stmt.setNull(3, Types.INTEGER)
		} else {
			stmt.setInt(3, parent.id)
		}
		stmt.setBoolean(4, showComments)
		stmt.setDouble(5, objectOrder)
		stmt.setString(6, content)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE Page SET title = ?, parent_id = ?, showComments = ?, objectOrder = ?, content = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, title)
		if (parent == null) {
			stmt.setNull(2, Types.INTEGER)
		} else {
			stmt.setInt(2, parent.id)
		}
		stmt.setBoolean(3, showComments)
		stmt.setDouble(4, objectOrder)
		stmt.setString(5, content)
		stmt.setInt(6, id)
		stmt.executeUpdate()
	}

	@Transient
	override val objectSettings = new ObjectSettings("fa-file-text-o")


}