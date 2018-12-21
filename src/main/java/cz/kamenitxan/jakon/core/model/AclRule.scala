package cz.kamenitxan.jakon.core.model

import java.sql.{SQLException, Statement}

import cz.kamenitxan.jakon.core.model.Dao.{Crud, DBHelper}
import javax.persistence._
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField

import scala.beans.BeanProperty

/**
  * Created by TPa on 30.04.18.
  */
@Entity
class AclRule(u: Unit = ()) extends JakonObject(childClass = classOf[JakonUser].getName) {
	@BeanProperty
	@Column
	@JakonField(searched = true, listOrder = 0)
	var name: String = ""
	@BeanProperty
	@Column
	@JakonField(listOrder = 1)
	var masterAdmin: Boolean = false
	@BeanProperty
	@Column
	@JakonField(listOrder = 2)
	var adminAllowed: Boolean = false
	@BeanProperty
	@ElementCollection
	@JakonField
	var allowedControllers: java.util.List[String] = new java.util.ArrayList[String]()
	@BeanProperty
	@ElementCollection
	@JakonField
	var allowedFrontendPrefixes: java.util.List[String] = new java.util.ArrayList[String]()

	def this() = this(u = ())

	override def create(): Int = {
		val jid = super.create()
		val sql = "INSERT INTO AclRule (id, name, masterAdmin, adminAllowed) VALUES (?, ?, ?, ?)"
		val stmt = DBHelper.getPreparedStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setBoolean(3, masterAdmin)
		stmt.setBoolean(4, adminAllowed)
		executeInsert(stmt)
	}

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")


	override def toString = s"AclRule($name)"
}
