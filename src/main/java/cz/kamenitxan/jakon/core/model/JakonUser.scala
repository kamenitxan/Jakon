package cz.kamenitxan.jakon.core.model

import javax.persistence._

import cz.kamenitxan.jakon.webui.Authentication.hashPassword
import cz.kamenitxan.jakon.webui.entity.JakonField
import cz.kamenitxan.jakon.webui.{Authentication, ObjectSettings}

import scala.beans.BeanProperty


/**
  * Created by TPa on 31.08.16.
  */
@Entity
class JakonUser(u: Unit = ()) extends JakonObject(childClass = classOf[JakonUser].getName) {

	@BeanProperty @Column var username: String = ""
	@BeanProperty @Column var email: String = ""
	@BeanProperty @Column var firstName: String = ""
	@BeanProperty @Column var lastName: String = ""
	@BeanProperty @Column var password: String = ""


	def this() = this(u=())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(null, null)

	override def create(): Unit = {
		Authentication.createUser(this)
	}

	override def update(): Unit = {
		this.password = hashPassword(this.password)
		super.update()
	}
}
