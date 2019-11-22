package cz.kamenitxan.jakon.core.task

import java.io.{File, IOException}
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, UserDefinedFileAttributeView}
import java.sql.Connection
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{FileType, JakonFile}
import cz.kamenitxan.jakon.core.service.{JakonFileService, UserService}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.controler.impl.FileManagerControler.REPOSITORY_BASE_PATH

class FileManagerConsistencyTestTask extends AbstractTask(classOf[FileManagerConsistencyTestTask].getSimpleName, 1, TimeUnit.HOURS) {

	private val FILE_ATTR_NAME = "jakonFileId"
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
					visitFileOrDirectory(dir, attrs, realPath, files)
				}

				@throws[IOException]
				override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
					visitFileOrDirectory(file, attrs, realPath, files)
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
	private def visitFileOrDirectory(file: Path, attrs: BasicFileAttributes, realPath: Path, files: List[JakonFile])(implicit conn: Connection) = {
		Logger.debug(s"Visiting $file")
		val fileName = file.toString.substring(realPath.toString.length)
		val fileId = getIdFromAttrs(file)
		if (fileId.isDefined) {
			val jakonFile = files.find(f => f.id == fileId.get)
			if (jakonFile.isDefined) {
				jakonFile.get.mappedToFs = true
			} else {
				Logger.error(s"File with id=${fileId.get} found on FS and not in DB")
				val jakonFile = createJakonFile(file)
				writeIdToAttrs(file, jakonFile.id)
			}
		} else {
			val jakonFile = files.find(f => f.path + f.name == fileName).getOrElse({
				createJakonFile(file)
			})
			jakonFile.mappedToFs = true
			writeIdToAttrs(file, jakonFile.id)
		}
		FileVisitResult.CONTINUE
	}

	private def createJakonFile(file: Path)(implicit conn: Connection) = {
		val jf = new JakonFile()
		jf.fileType = if (Files.isDirectory(file)) FileType.FOLDER else FileType.FILE
		jf.name = file.getFileName.toString
		jf.path = file.getParent.toString
		jf.created = LocalDateTime.now()
		jf.author = UserService.getMasterAdmin()
		jf.create()
		jf
	}

	private def getIdFromAttrs(file: Path): Option[Int] = {
		val userDefView = Files.getFileAttributeView(file, classOf[UserDefinedFileAttributeView])
		if(!userDefView.list().contains(FILE_ATTR_NAME)) {
			return Option.empty
		}

		val attrValue: ByteBuffer = ByteBuffer.allocate(10)
		userDefView.read(FILE_ATTR_NAME, attrValue)
		attrValue.flip
		val id = Charset.defaultCharset.decode(attrValue).toString
		id.toOptInt
	}

	private def writeIdToAttrs(file: Path, id: Int) = {
		val userDefView = Files.getFileAttributeView(file, classOf[UserDefinedFileAttributeView])
		userDefView.write(FILE_ATTR_NAME, Charset.defaultCharset.encode(id.toString))
	}
}
