package cz.kamenitxan.jakon.core.model
import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

class JakonFile extends JakonObject {

	@JakonField
	var name: String = scala.compiletime.uninitialized
	@JakonField
	var path: String = scala.compiletime.uninitialized
	@ManyToOne
	@JakonField
	var author: JakonUser = scala.compiletime.uninitialized
	@JakonField
	var created: LocalDateTime = scala.compiletime.uninitialized
	@JakonField(disabled = true)
	var fileType: FileType = scala.compiletime.uninitialized

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
