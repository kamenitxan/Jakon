package cz.kamenitxan.jakon.core.model
import java.time.LocalDateTime
import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.webui.ObjectSettings

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
	@JakonField(disabled = true)
	var fileType: FileType = _

	@transient
	var mappedToFs: Boolean = false

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-file")

	// TODO: delete also file on FS
	override def delete(): Unit = super.delete()
}
