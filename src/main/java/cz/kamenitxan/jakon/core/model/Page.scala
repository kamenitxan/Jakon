package cz.kamenitxan.jakon.core.model

import java.io.StringWriter
import java.sql.{Connection, Statement, Types}
import java.util.regex.Pattern

import cz.kamenitxan.jakon.core.function.FunctionHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.json.Json
import javax.persistence._

import scala.beans.BeanProperty

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
@Entity
class Page(u: Unit = ()) extends JakonObject(classOf[Page].getName) with Ordered {
	@BeanProperty @Column @JakonField
	var title:String = ""
	@Column @JakonField
	var content:String = ""
	@BeanProperty @OneToOne @JakonField(inputTemplate = "String")
	var parent:Page = _
	@BeanProperty @Column @JakonField
	var showComments:Boolean = false
	@Column(nullable = false) @JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	override var objectOrder: Double = _
	@Transient @JakonField(listOrder = -96)
	override var visibleOrder: Int = _

	def this() = this(u=())

	def setContent(content: String): Unit = this.content = content

	def getContent: String = {
		if (content == null) return ""
		// TODO: parsovani funkci
		// (\{)((?:[a-z][a-z]+)).*?(\})
		val p = Pattern.compile("(\\{)((?:[a-z][a-z]+))(.*?)(\\})")
		val m = p.matcher(content)
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

	override def getUrl: String = "/page/" + title.replaceAll(" ", "_").toLowerCase


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

	override def getObjectOrder: Double = objectOrder

	override def setObjectOrder(objectOrder: Double): Unit = this.objectOrder = objectOrder

	override def toJson: String = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(super.getId).write(title).write(getContent).write(parent.id).writeEnd
		generator.close()
		writer.toString
	}

	@Transient
	override val objectSettings = new ObjectSettings("fa-file-text-o")


}