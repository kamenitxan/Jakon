package cz.kamenitxan.jakon.core.model

import java.sql.{Connection, Statement}

import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence._

/**
  * Created by TPa on 30.04.18.
  */
@Entity
class AclRule(u: Unit = ()) extends JakonObject(childClass = classOf[JakonUser].getName) {
	@JakonField(searched = true, listOrder = 0)
	var name: String = ""
	@JakonField(listOrder = 1)
	var masterAdmin: Boolean = false
	@JakonField(listOrder = 2)
	var adminAllowed: Boolean = false
	@JakonField
	var allowedControllers: java.util.List[String] = new java.util.ArrayList[String]()
	@JakonField
	var allowedFrontendPrefixes: java.util.List[String] = new java.util.ArrayList[String]()

	def this() = this(u = ())

	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO AclRule (id, name, masterAdmin, adminAllowed) VALUES (?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setBoolean(3, masterAdmin)
		stmt.setBoolean(4, adminAllowed)
		executeInsert(stmt)
	}

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")


	override def toString = s"AclRule($name)"

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE AclRule SET name = ?, masterAdmin = ?, adminAllowed = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setBoolean(2, masterAdmin)
		stmt.setBoolean(3, adminAllowed)
		stmt.setInt(4, jid)
		stmt.executeUpdate()
	}
}
