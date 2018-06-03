package cz.kamenitxan.jakon.core.model

import javax.persistence._

import cz.kamenitxan.jakon.webui.controler.impl.Authentication.hashPassword
import cz.kamenitxan.jakon.webui.entity.JakonField
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.controler.impl.Authentication

import scala.beans.BeanProperty


/**
  * Created by TPa on 31.08.16.
  */
@Entity
class JakonUser(u: Unit = ()) extends JakonObject(childClass = classOf[JakonUser].getName) with Serializable {

	@BeanProperty @Column @JakonField var username: String = ""
	@BeanProperty @Column @JakonField var email: String = ""
	@BeanProperty @Column @JakonField var firstName: String = ""
	@BeanProperty @Column @JakonField var lastName: String = ""
	@BeanProperty @Column @JakonField var password: String = ""
	@BeanProperty @Column @JakonField var enabled: Boolean = false
	@BeanProperty
	@ManyToOne
	@JakonField(required = true) var acl: AclRule = _

	def this() = this(u=())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-user")

	override def create(): Int = {
		Authentication.createUser(this).id
	}

}
