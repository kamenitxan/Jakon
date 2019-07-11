package cz.kamenitxan.jakon.core.task

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.attribute.{BasicFileAttributes, UserDefinedFileAttributeView}
import java.nio.file._
import java.sql.Connection
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.service.UserService
import cz.kamenitxan.jakon.core.model.{FileType, JakonFile}
import cz.kamenitxan.jakon.core.service.JakonFileService
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.controler.impl.FileManagerControler.REPOSITORY_BASE_PATH
import org.slf4j.{Logger, LoggerFactory}

class FileManagerConsistencyTestTask extends AbstractTask(classOf[FileManagerConsistencyTestTask].getSimpleName, 1, TimeUnit.HOURS) {

	private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	private val FILE_ATTR_NAME = "jakonFileId"

	override def start(): Unit = {
		val realPath = Paths.get(REPOSITORY_BASE_PATH, "/basePath")
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
				logger.warn(s"JakonFile(id=${f.id}, name=${f.name}) not found on FS")
				f.delete()
			})
		})
	}

	@throws[IOException]
	private def visitFileOrDirectory(file: Path, attrs: BasicFileAttributes, realPath: Path, files: List[JakonFile])(implicit conn: Connection) = {
		logger.debug(s"Visiting $file")
		val fileName = file.toString.substring(realPath.toString.length)
		val fileId = getIdFromAttrs(file)
		if (fileId.isDefined) {
			val jakonFile = files.find(f => f.id == fileId.get)
			if (jakonFile.isDefined) {
				jakonFile.get.mappedToFs = true
			} else {
				logger.error(s"File with id=${fileId.get} found on FS and not in DB")
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
