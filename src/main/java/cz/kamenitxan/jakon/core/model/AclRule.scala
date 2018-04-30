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
	var allowedControllers: java.util.List[String] = _
	@BeanProperty
	@ElementCollection
	@JakonField
	var allowedFrontendPrefixes: java.util.List[String] = _

	def this() = this(u = ())

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")


	override def toString = s"AclRule($name)"
}
