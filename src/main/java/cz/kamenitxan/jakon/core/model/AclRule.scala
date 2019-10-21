package cz.kamenitxan.jakon.core.model

import java.sql.{Connection, Statement}

import cz.kamenitxan.jakon.core.database.converters.StringListConverter
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence._

/**
  * Created by TPa on 30.04.18.
  */
@Entity
class AclRule extends JakonObject(childClass = classOf[AclRule].getName) {
	@NotEmpty
	@JakonField(searched = true, listOrder = 0)
	var name: String = ""
	@JakonField(listOrder = 1)
	var masterAdmin: Boolean = false
	@JakonField(listOrder = 2)
	var adminAllowed: Boolean = false
	@JakonField(converter = classOf[StringListConverter])
	var allowedControllers: java.util.List[String] = new java.util.ArrayList[String]()
	@JakonField(converter = classOf[StringListConverter])
	var allowedFrontendPrefixes: java.util.List[String] = new java.util.ArrayList[String]()


	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO AclRule (id, name, masterAdmin, adminAllowed, allowedControllers, allowedFrontendPrefixes) VALUES (?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setBoolean(3, masterAdmin)
		stmt.setBoolean(4, adminAllowed)
		stmt.setString(5, new StringListConverter().convertToDatabaseColumn(allowedControllers))
		stmt.setString(6, new StringListConverter().convertToDatabaseColumn(allowedFrontendPrefixes))
		executeInsert(stmt)
	}

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")

	override def toString = s"AclRule($name)"

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE AclRule SET name = ?, masterAdmin = ?, adminAllowed = ?, allowedControllers = ?, allowedFrontendPrefixes = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setBoolean(2, masterAdmin)
		stmt.setBoolean(3, adminAllowed)
		stmt.setString(4, new StringListConverter().convertToDatabaseColumn(allowedControllers))
		stmt.setString(5, new StringListConverter().convertToDatabaseColumn(allowedFrontendPrefixes))
		stmt.setInt(6, jid)
		stmt.executeUpdate()
	}
}
