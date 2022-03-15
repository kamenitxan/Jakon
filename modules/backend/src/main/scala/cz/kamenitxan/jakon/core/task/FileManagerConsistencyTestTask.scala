package cz.kamenitxan.jakon.core.task

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonFile
import cz.kamenitxan.jakon.core.service.{JakonFileService, UserService}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController.REPOSITORY_BASE_PATH

import java.io.{File, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.sql.Connection
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class FileManagerConsistencyTestTask extends AbstractTask(1, TimeUnit.HOURS) {

	private val BASE_DIR = "/basePath"

	override def start(): Unit = {
		val osName = System.getProperty("os.name").toLowerCase
		val isMacOs = osName.startsWith("mac os x")
		if (isMacOs) {
			// https://bugs.openjdk.java.net/browse/JDK-8030048
			// (fs) Support UserDefinedFileAttributeView/extended attributes on OS X / HFS+
			Logger.warn(s"FileManagerConsistencyTest is not supported on Mac OS X")
			return
		}

		checkUploadDirectory()
		val realPath = Paths.get(REPOSITORY_BASE_PATH, BASE_DIR)
		DBHelper.withDbConnection(implicit conn => {
			val files = JakonFileService.getAll

			Files.walkFileTree(realPath, new SimpleFileVisitor[Path]() {
				override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
					visitFileOrDirectory(dir, files)
				}

				@throws[IOException]
				override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
					visitFileOrDirectory(file, files)
				}
			})
			files.filter(f => !f.mappedToFs).foreach(f => {
				Logger.warn(s"JakonFile(id=${f.id}, name=${f.name}) not found on FS")
				f.delete()
			})
		})
	}

	private def checkUploadDirectory(): Unit = {
		val dir = new File(REPOSITORY_BASE_PATH + BASE_DIR)
		if (!dir.exists()) {
			val res = dir.mkdirs()
			if (res) {
				Logger.info(s"Upload directory created: $dir")
			} else {
				Logger.warn(s"Failed to create upload directory: $dir")
			}
		}
	}

	@throws[IOException]
	private def visitFileOrDirectory(file: Path, files: Seq[JakonFile])(implicit conn: Connection) = {
		Logger.debug(s"Visiting $file")
		val path = file.getParent.toString
		val fileName = file.getFileName.toString

		val jakonFile = files.find(f => f.name == fileName && path == f.path)
		if (jakonFile.isDefined) {
			val jf = jakonFile.get
			jf.mappedToFs = true
			val fileType = FileManagerController.getFileType(file)
			if (jf.fileType != fileType) {
				jf.fileType = fileType
				jf.update()
			}
		} else {
			val jakonFile = createJakonFile(file)
			jakonFile.mappedToFs = true
		}

		FileVisitResult.CONTINUE
	}

	private def createJakonFile(file: Path)(implicit conn: Connection) = {
		val jf = new JakonFile()
		jf.fileType = FileManagerController.getFileType(file)
		jf.name = file.getFileName.toString
		jf.path = file.getParent.toString
		jf.created = LocalDateTime.now()
		jf.author = UserService.getMasterAdmin()
		jf.create()
		Logger.error(s"JakonFile(id=${jf.id}, name=${jf.name}) created in DB")
		jf
	}

}
