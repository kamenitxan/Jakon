package cz.kamenitxan.jakon.core.model

import java.sql.{Statement, Types}

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence._

import scala.beans.BeanProperty


/**
  * Created by TPa on 31.08.16.
  */
@Entity
class JakonUser(u: Unit = ()) extends JakonObject(childClass = classOf[JakonUser].getName) with Serializable {

	@BeanProperty @Column @JakonField var username: String = ""
	@BeanProperty @Column @JakonField var email: String = ""
	@BeanProperty @Column @JakonField var emailConfirmed: Boolean = false
	@BeanProperty @Column @JakonField var firstName: String = ""
	@BeanProperty @Column @JakonField var lastName: String = ""
	@BeanProperty
	@Column
	@JakonField(shownInList = false)
	var password: String = ""
	@BeanProperty @Column @JakonField var enabled: Boolean = false
	@BeanProperty
	@ManyToOne
	@JakonField(required = true) var acl: AclRule = _

	def this() = this(u=())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-user")

	override def create(): Int = {
		this.password = Authentication.hashPassword(this.password)
		val jid = super.create()
		val conn = DBHelper.getConnection
		val sql = "INSERT INTO JakonUser (id, username, email, emailConfirmed, firstName, lastName, password, enabled, acl_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, username)
		stmt.setString(3, email)
		stmt.setBoolean(4, emailConfirmed)
		stmt.setString(5, firstName)
		stmt.setString(6, lastName)
		stmt.setString(7, password)
		stmt.setBoolean(8, enabled)
		if (acl != null) {
			stmt.setInt(9, acl.id)
		} else {
			stmt.setNull(9, Types.INTEGER)
		}

		val id = executeInsert(stmt)
		conn.close()
		id
	}

	override def toString: String = {
		s"JakonUser(id: $id)"
	}
}
