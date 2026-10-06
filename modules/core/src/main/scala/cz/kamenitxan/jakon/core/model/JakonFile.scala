package cz.kamenitxan.jakon.core.model
import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

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

	override def delete(): Unit = {
		try {
			val fsPath = Paths.get(path, name)
			if (!Files.deleteIfExists(fsPath)) {
				Logger.warn(s"JakonFile(id=$id, name=$name) not found on FS at $fsPath, skipping FS delete")
			}
		} catch {
			case e: Exception => Logger.error(s"Failed to delete JakonFile(id=$id, name=$name) on FS", e)
		}
		super.delete()
	}
}
