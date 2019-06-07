package cz.kamenitxan.jakon.core.model


import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.{Entity, Transient}

@Entity
class EmailConfirmation(u: Unit = ()) extends JakonObject(childClass = classOf[EmailConfirmation].getName) {


	def this() = this(u = ())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

}
