package cz.kamenitxan.jakon.core.model
import java.time.LocalDateTime

import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.ManyToOne

class JakonFile extends JakonObject {

	@JakonField
	var name: String = _
	@JakonField
	var path: String = _
	@ManyToOne
	@JakonField
	var author: JakonUser = _
	@JakonField
	var created: LocalDateTime = _
	@JakonField
	var fileType: FileType = _

	@transient
	var mappedToFs: Boolean = false

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-file")


}
