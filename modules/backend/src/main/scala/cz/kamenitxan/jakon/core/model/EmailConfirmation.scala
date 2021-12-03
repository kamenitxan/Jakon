package cz.kamenitxan.jakon.core.model


import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.Transient

class EmailConfirmation extends JakonObject {

	@Transient
	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

}
