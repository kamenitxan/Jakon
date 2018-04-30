package cz.kamenitxan.jakon.core.model

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
	@JakonField(searched = true)
	var name: String = ""
	@BeanProperty
	@Column
	@JakonField
	var masterAdmin: Boolean = false
	@BeanProperty
	@Column
	@JakonField
	var adminAllowed: Boolean = false
	@BeanProperty
	@ElementCollection
	@JakonField
	var allowedControllers: java.util.List[String] = _
	@BeanProperty
	@ElementCollection
	@JakonField
	var allowedFrontendPrefixes: java.util.List[String] = _

	def this() = this(u = ())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")
}
