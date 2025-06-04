package cz.kamenitxan.jakon.webui.controller.impl

import com.google.gson.*
import cz.kamenitxan.jakon.core.model.{FileType, JakonFile}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.controller.AbstractController
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController.getManager
import cz.kamenitxan.jakon.webui.entity.FileManagerMode
import io.javalin.http.Context
import jakarta.mail.internet.MimeUtility
import jakarta.servlet.ServletException
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.commons.fileupload2.core.{DiskFileItemFactory, FileUploadException}
import org.apache.commons.fileupload2.jakarta.JakartaServletDiskFileUpload
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils

import java.io.*
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.{FileAlreadyExistsException, FileSystems, FileVisitResult, Files, Path, Paths, SimpleFileVisitor, StandardCopyOption}
import java.nio.file.attribute.{BasicFileAttributes, PosixFileAttributeView, PosixFilePermissions}
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util
import java.util.Date
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.boundary
import boundary.break

/**
	* This controller serve angular-filemanager call<br>
	*
	* that catch all request to path /fm&#47;*<br>
	* in angular-filemanager-master/index.html uncomment links to js files<br>
	* in my assest/config.js I have :
	*
	* <pre>
	* listUrl : "/fm/listUrl",
	* uploadUrl : "/fm/uploadUrl",
	* renameUrl : "/fm/renameUrl",
	* copyUrl : "/fm/copyUrl",
	* removeUrl : "/fm/removeUrl",
	* editUrl : "/fm/editUrl",
	* getContentUrl : "/fm/getContentUrl",
	* createFolderUrl : "/fm/createFolderUrl",
	* downloadFileUrl : "/fm/downloadFileUrl",
	* compressUrl : "/fm/compressUrl",
	* extractUrl : "/fm/extractUrl",
	* permissionsUrl : "/fm/permissionsUrl",
	* </pre>
	*
	* <b>NOTE:</b><br>
	* Does NOT manage 'preview' parameter in download<br>
	* Compress and expand are NOT implemented<br>
	*
	* @author Paolo Biavati https://github.com/paolobiavati (java servlet version)
	* @author Kamenitxan this implementation
	*/
class FileManagerController extends AbstractController {
	override val template: String = "objects/fileManager"

	override def render(ctx: Context): cz.kamenitxan.jakon.webui.Context = getManager(ctx)

	override def name(): String = "FILES"

	override def path(): String = "files"

	override val icon: String = "fa-files-o"
}

object FileManagerController {
	val REPOSITORY_BASE_PATH = "upload"
	private val DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z" // (Wed, 4 Jul 2001 12:08:56)
	private val enabledAction: util.Map[FileManagerMode, Boolean] = new util.HashMap[FileManagerMode, Boolean]
	private val JSON_RESPONSE_TYPE = "application/json;charset=UTF-8"
	private val AlreadyExists = " already exits!"

	val gson: Gson = new Gson()

	init()

	def getManager(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), "objects/fileManager")
	}

	def getManagerFrame(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), "objects/fileManagerFrame")
	}

	def executeGet(ctx: Context): Unit = {
		val request = ctx.req()
		val response = ctx.res()

		val action = request.getParameter("action")
		if ("download" == action) {
			val path = request.getParameter("path")
			val file = new File(REPOSITORY_BASE_PATH, path)
			if (!file.isFile) { // if not a file, it is a folder, show this error.
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found")
				return
			}
			response.setHeader("Content-Type", "application/force-download")
			response.setHeader("Content-Disposition", "attachment; filename=\"" + MimeUtility.encodeWord(file.getName) + "\"")

			val channel = Files.newByteChannel(file.toPath)
			try {
				val buffer = new Array[Byte](256 * 1024)
				val byteBuffer = ByteBuffer.wrap(buffer)
				var length = 0
				while ( {
					length = channel.read(byteBuffer)
					length
				} != -1) {
					response.getOutputStream.write(buffer, 0, length)
					byteBuffer.clear
				}
			} catch {
				case ex: IOException =>
					Logger.error(ex.getMessage, ex)
					throw ex
			} finally {
				response.getOutputStream.flush()
				if (channel != null) {
					channel.close()
				}
			}

		} else if ("downloadMultiple" == action) {
			val toFilename = request.getParameter("toFilename")
			val items = request.getParameterValues("items[]")
			val baos = new ByteArrayOutputStream
			var zos: ZipOutputStream = null
			try {
				zos = new ZipOutputStream(new BufferedOutputStream(baos))
				for (item <- items) {
					val path = Paths.get(REPOSITORY_BASE_PATH, item)
					if (Files.exists(path)) {
						val zipEntry = new ZipEntry(path.getFileName.toString)
						zos.putNextEntry(zipEntry)
						val buffer = new Array[Byte](2048)

						val bis = new BufferedInputStream(Files.newInputStream(path))
						try {
							var bytesRead = 0
							while ( {
								bytesRead = bis.read(buffer)
								bytesRead
							} != -1) {
								zos.write(buffer, 0, bytesRead)
							}
						} finally {
							zos.closeEntry()
							if (bis != null) {
								bis.close()
							}
						}

					}
				}
			} finally {
				if (zos != null) {
					zos.close()
				}
			}
			response.setContentType("application/zip")
			response.setHeader("Content-Disposition", "inline; filename=\"" + MimeUtility.encodeWord(toFilename) + "\"")
			val output = new BufferedOutputStream(response.getOutputStream)
			output.write(baos.toByteArray)
			output.flush()
		}


	}

	def executePost(ctx: Context): Unit = {
		try { // if request contains multipart-form-data
			if (ctx.isMultipart) {
				if (isSupportFeature(FileManagerMode.UPLOAD)) {
					uploadFile(ctx.req(), ctx.res())
				} else {
					setError(new IllegalAccessError(notSupportFeature(FileManagerMode.UPLOAD).get("result").getAsJsonObject.get("error").getAsString), ctx.res())
				}
			} else { // all other post request has jspn params in body}
				fileOperation(ctx.req(), ctx.res())
			}
		} catch {
			case ex@(_: ServletException | _: IOException) =>
				Logger.error(ex.getMessage, ex)
				setError(ex, ctx.res())
		}
	}

	def init(): Unit = {
		enabledAction.put(FileManagerMode.RENAME, true)
		enabledAction.put(FileManagerMode.MOVE, true)
		enabledAction.put(FileManagerMode.REMOVE, true)
		enabledAction.put(FileManagerMode.EDIT, true)
		enabledAction.put(FileManagerMode.CREATE_FOLDER, true)
		enabledAction.put(FileManagerMode.CHANGE_PERMISSIONS, false)
		enabledAction.put(FileManagerMode.COMPRESS, true)
		enabledAction.put(FileManagerMode.EXTRACT, false)
		enabledAction.put(FileManagerMode.COPY, true)
		enabledAction.put(FileManagerMode.UPLOAD, true)
	}


	@throws[IOException]
	private def fileOperation(request: HttpServletRequest, response: HttpServletResponse): Unit = {
		var responseJsonObject: JsonObject = null
		try {
			val br = request.getReader
			val str = LazyList.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
			br.close()

			val params: JsonObject = JsonParser.parseString(str).getAsJsonObject;
			val mode: FileManagerMode = FileManagerMode.ofAction(params.get("action").getAsString)
			responseJsonObject = mode match {
				case FileManagerMode.CREATE_FOLDER =>
					executeIfSupported(mode, params, p => createFolder(p))
				case FileManagerMode.CHANGE_PERMISSIONS =>
					executeIfSupported(mode, params, p => null) //changePermissions(p))
				case FileManagerMode.COMPRESS =>
					executeIfSupported(mode, params, p => compress(p))
				case FileManagerMode.COPY =>
					executeIfSupported(mode, params, p => copy(p))
				case FileManagerMode.REMOVE =>
					executeIfSupported(mode, params, p => remove(p))
				case FileManagerMode.GET_CONTENT =>
					getContent(params)
				case FileManagerMode.EDIT => // get content
					executeIfSupported(mode, params, p => editFile(p))
				case FileManagerMode.EXTRACT =>
					executeIfSupported(mode, params, p => null) //extract(p))
				case FileManagerMode.LIST =>
					list(params)
				case FileManagerMode.RENAME =>
					executeIfSupported(mode, params, p => rename(p))
				case FileManagerMode.MOVE =>
					executeIfSupported(mode, params, p => move(p))
				case _ =>
					throw new UnsupportedOperationException("not implemented")
			}
			if (responseJsonObject == null) responseJsonObject = error("generic error : responseJsonObject is null")
		} catch {
			case e@(_: IOException | _: ServletException) =>
				responseJsonObject = error(e.getMessage)
		}
		response.setContentType(JSON_RESPONSE_TYPE)
		val out = response.getWriter
		out.print(responseJsonObject)
		out.flush()
	}

	private def executeIfSupported(mode: FileManagerMode, params: JsonObject, fun: JsonObject => JsonObject): JsonObject = {
		if (isSupportFeature(mode)) {
			fun(params)
		} else {
			notSupportFeature(mode)
		}
	}

	private def isSupportFeature(mode: FileManagerMode) = {
		Logger.debug(s"check support $mode")
		if (SystemUtils.IS_OS_WINDOWS) {
			Logger.error("File manager is not available on Windows")
			false
		} else {
			enabledAction.get(mode)
		}
	}

	private def notSupportFeature(mode: FileManagerMode): JsonObject = error("This implementation not support " + mode + " feature")


	/**
		* URL: $config.uploadUrl, Method: POST, Content-Type: multipart/form-data
		* Unlimited file upload, each item will be enumerated as file-1, file-2, etc.
		* [$config.uploadUrl]?destination=/public_html/image.jpg&file-1={..}&file-2={...}
		*/
	@throws[ServletException]
	private def uploadFile(request: HttpServletRequest, response: HttpServletResponse): Unit = {
		if (isSupportFeature(FileManagerMode.UPLOAD)) {
			Logger.debug("upload now")
			try {
				var destination: String = null
				val files = new util.HashMap[String, InputStream]

				val factory = DiskFileItemFactory.builder().get()

				val sfu: JakartaServletDiskFileUpload = new JakartaServletDiskFileUpload(factory)
				//sfu.setHeaderEncoding("UTF-8")
				val items = sfu.parseRequest(request).asScala
				for (item <- items) {
					if (item.isFormField) { // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
						if ("destination" == item.getFieldName) {
							destination = item.getString()
						}
					}
					else { // Process form file field (input type="file").
						files.put(item.getName, item.getInputStream)
					}
				}
				if (files.isEmpty) {
					Logger.debug("file size  = 0")
					throw new Exception("file size  = 0")
				} else {
					files.entrySet.forEach(fileEntry => {
						val path = Paths.get(REPOSITORY_BASE_PATH + destination, fileEntry.getKey)
						if (!write(fileEntry.getValue, path)) {
							Logger.debug("write error")
							throw new Exception("write error")
						}
						val jf = new JakonFile()
						jf.fileType = FileManagerController.getFileType(path)
						jf.name = path.getFileName.toString
						jf.path = path.getParent.toString
						jf.created = LocalDateTime.now()
						jf.author = PageContext.getInstance().getLoggedUser.orNull
						jf.create()
						Logger.info(s"JakonFile(id=${jf.id}, name=${jf.name}) created in DB")
					})
					var responseJsonObject: JsonObject = null
					responseJsonObject = this.success()
					response.setContentType(JSON_RESPONSE_TYPE)
					val out: PrintWriter = response.getWriter
					out.print(responseJsonObject)
					out.flush()
				}
			} catch {
				case e: FileUploadException =>
					Logger.error("Cannot parse multipart request: DiskFileItemFactory.parseRequest", e)
					throw new ServletException("Cannot parse multipart request: DiskFileItemFactory.parseRequest", e)
				case e: IOException =>
					Logger.error("Cannot parse multipart request: item.getInputStream")
					throw new ServletException("Cannot parse multipart request: item.getInputStream", e)
				case e: Exception =>
					Logger.error("Cannot write file", e)
					throw new ServletException("Cannot write file", e)
			}
		} else {
			throw new ServletException(notSupportFeature(FileManagerMode.UPLOAD).get("error").getAsString)
		}
	}

	private def list(params: JsonObject) = {
		try {
			val onlyFolders = "true".equalsIgnoreCase(Option.apply(params.get("onlyFolders")).map(_.getAsString).getOrElse("false"))
			val path = params.get("path").getAsString
			Logger.debug(s"list path: Paths.get('$REPOSITORY_BASE_PATH', '$path'), onlyFolders: $onlyFolders")
			val resultList = new util.ArrayList[JsonObject]
			val directoryStream = Files.newDirectoryStream(Paths.get(REPOSITORY_BASE_PATH, path))
			try {
				val dt = new SimpleDateFormat(DATE_FORMAT)
				directoryStream.asScala.filterNot(p => {
					val attrs = Files.readAttributes(p, classOf[BasicFileAttributes])
					onlyFolders && !attrs.isDirectory
				}).foreach(p => {
					val attrs = Files.readAttributes(p, classOf[BasicFileAttributes])
					val el = new JsonObject
					el.add("name", JsonPrimitive(p.getFileName.toString))
					el.add("rights", JsonPrimitive(getPermissions(p)))
					el.add("date", JsonPrimitive(dt.format(new Date(attrs.lastModifiedTime.toMillis))))
					el.add("size", JsonPrimitive(java.lang.Long.valueOf(attrs.size)))
					el.add("type", if (attrs.isDirectory) JsonPrimitive("dir") else JsonPrimitive("file"))
					resultList.add(el)
				})

			} catch {
				case ex: IOException => Logger.error("Error while listing files", ex)
			} finally {
				if (directoryStream != null) directoryStream.close()
			}

			val arr = new JsonArray()
			resultList.forEach(arr.add(_))

			val json = new JsonObject
			json.add("result", arr)
			json
		} catch {
			case e: Exception =>
				Logger.error("list:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def createFolder(params: JsonObject) = try {
		val path = Paths.get(REPOSITORY_BASE_PATH, params.get("newPath").getAsString)
		Logger.debug(s"createFolder path: $path")
		val createdDirectory = Files.createDirectories(path)
		val fo = new JakonFile()
		fo.fileType = FileType.FOLDER
		fo.name = createdDirectory.getFileName.toString
		fo.path = createdDirectory.getParent.toString
		fo.created = LocalDateTime.now()
		fo.author = PageContext.getInstance().getLoggedUser.orNull
		fo.create()
		success()
	} catch {
		case _: FileAlreadyExistsException =>
			success()
		case e: IOException =>
			Logger.error("createFolder:" + e.getMessage, e)
			error(e.getMessage)
	}

	/*private def changePermissions(params: JsonObject) = {
		try {
			val paths = params.get("items").asInstanceOf[JsonArray]
			val perms = params.getAsString("perms") // "rw-r-x-wx"
			val permsCode = params.getAsString("permsCode") // "653"
			val recursive = "true".equalsIgnoreCase(params.getAsString("recursive"))
			for (path <- paths) {
				Logger.debug(s"changepermissions path: $path, perms: $perms, permsCode: $permsCode, recursive: $recursive")
				val f = Paths.get(REPOSITORY_BASE_PATH, path.toString).toFile
				setPermissions(f, perms, recursive)
			}
			success()
		} catch {
			case e: IOException =>
				Logger.error("changepermissions:" + e.getMessage, e)
				error(e.getMessage)
		}
	}*/

	// TODO: presunout i v DB
	private def move(params: JsonObject): JsonObject = {
		boundary {
			try {
				val paths = params.get("items").asInstanceOf[JsonArray].asScala
				val newpath = Paths.get(REPOSITORY_BASE_PATH, params.get("newPath").getAsString)
				paths.foreach(obj => {
					val path = Paths.get(REPOSITORY_BASE_PATH, obj.getAsString)
					val mpath = newpath.resolve(path.getFileName)
					Logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
					if (Files.exists(mpath)) {
						break(error(mpath.toString + AlreadyExists))
					}
				})
				paths.foreach(obj => {
					val path = Paths.get(REPOSITORY_BASE_PATH, obj.getAsString)
					val mpath = newpath.resolve(path.getFileName)
					Files.move(path, mpath, StandardCopyOption.REPLACE_EXISTING)
				})
				success()
			} catch {
				case e: IOException =>
					Logger.error("move:" + e.getMessage, e)
					error(e.getMessage)
			}
		}
	}

	// TODO: prejmenovat i v DB
	private def rename(params: JsonObject) = {
		try {
			val path = params.get("item").getAsString
			val newpath = params.get("newItemPath").getAsString
			Logger.debug(s"rename from: $path to: $newpath")
			val srcFile = new File(REPOSITORY_BASE_PATH, path)
			val destFile = new File(REPOSITORY_BASE_PATH, newpath)
			if (srcFile.isFile) {
				FileUtils.moveFile(srcFile, destFile)
			}
			else {
				FileUtils.moveDirectory(srcFile, destFile)
			}
			success()
		} catch {
			case e: IOException =>
				Logger.error("rename:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	// TODO: smazat i v DB
	@throws[ServletException]
	private def remove(params: JsonObject): JsonObject = {
		val paths = params.get("items").asInstanceOf[JsonArray].asScala
		val error = new StringBuilder
		val sb = new StringBuilder
		paths.foreach(obj => {
			val path = Paths.get(REPOSITORY_BASE_PATH, obj.getAsString)
			if (!FileUtils.deleteQuietly(path.toFile)) {
				val errrMsg = if (error.nonEmpty) {
					"\n"
				} else {
					"Can't remove: \n/"
				}
				error.append(errrMsg).append(path.subpath(1, path.getNameCount).toString)
			} else {
				val msg = if (error.nonEmpty) "\n" else "\nBut remove remove: \n/"
				sb.append(msg).append(path.subpath(1, path.getNameCount).toString)
				Logger.debug(s"remove $path")
			}
		})
		if (error.nonEmpty) {
			if (sb.nonEmpty) {
				sb.append("\nPlease refresh this folder to list last result.")
			}
			throw new ServletException(error.toString + sb.toString)
		} else {
			success()
		}
	}

	private def getContent(params: JsonObject) = {
		try {
			val json = new JsonObject
			val item: String = params.get("item").getAsString
			json.add("result", JsonPrimitive(FileUtils.readFileToString(Paths.get(REPOSITORY_BASE_PATH, item).toFile, Charset.defaultCharset())))
			json
		} catch {
			case ex: IOException =>
				Logger.error("getContent:" + ex.getMessage, ex)
				error(ex.getMessage)
		}
	}

	private def editFile(params: JsonObject) = { // get content
		try {
			val path = params.get("item").getAsString
			Logger.debug(s"editFile path: $path")
			val srcFile = new File(REPOSITORY_BASE_PATH, path)
			val content = params.get("content").getAsString
			FileUtils.writeStringToFile(srcFile, content, Charset.defaultCharset())
			success()
		} catch {
			case e: IOException =>
				Logger.error("editFile:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def copy(params: JsonObject): JsonObject = {
		boundary {
			try {
				val paths = params.get("items").asInstanceOf[JsonArray].asScala
				val newpath = Paths.get(REPOSITORY_BASE_PATH, params.get("newPath").getAsString)

				val newFileName = params.get("singleFilename").getAsString
				paths.foreach(obj => {
					val path = if (newFileName == null) {
						Paths.get(REPOSITORY_BASE_PATH, obj.getAsString)
					} else {
						Paths.get(".", newFileName)
					}
					val mpath = newpath.resolve(path.getFileName)
					Logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
					if (Files.exists(mpath)) {
						break(error(mpath.toString + AlreadyExists))
					}
				})
				paths.foreach(obj => {
					val path = Paths.get(REPOSITORY_BASE_PATH, obj.getAsString)
					val mpath = newpath.resolve(if (newFileName == null) path.getFileName
					else Paths.get(".", newFileName).getFileName)
					Files.copy(path, mpath, StandardCopyOption.REPLACE_EXISTING)
				})
				success()
			}
			catch {
				case e: IOException =>
					Logger.error("copy:" + e.getMessage, e)
					error(e.getMessage)
			}
		}
	}

	private def compress(params: JsonObject): JsonObject = try {
		val paths = params.get("items").asInstanceOf[JsonArray].asScala.toSeq
		val paramDest = params.get("destination").getAsString
		val dest = Paths.get(REPOSITORY_BASE_PATH, paramDest)
		val zip = dest.resolve(params.get("compressedFilename").getAsString)
		if (Files.exists(zip)) {
			return error(zip.toString + AlreadyExists)
		}
		val env = new util.HashMap[String, String]
		env.put("create", "true")
		var zipped = false

		val appDir = dest.toAbsolutePath.toString.replace("upload/basePath", "")
		val zipfs = FileSystems.newFileSystem(URI.create(s"jar:file:$appDir" + zip.toString), env)
		try {
			paths.foreach(path => {
				val realPath = Paths.get(REPOSITORY_BASE_PATH, path.getAsString)
				if (Files.isDirectory(realPath)) Files.walkFileTree(Paths.get(REPOSITORY_BASE_PATH, path.getAsString), new SimpleFileVisitor[Path]() {
					@throws[IOException]
					override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
						Files.createDirectories(zipfs.getPath(dir.toString.substring(dest.toString.length)))
						FileVisitResult.CONTINUE
					}

					@throws[IOException]
					override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
						val pathInZipFile = zipfs.getPath(file.toString.substring(dest.toString.length))
						Logger.debug(s"compress: '$pathInZipFile'")
						Files.copy(file, pathInZipFile, StandardCopyOption.REPLACE_EXISTING)
						FileVisitResult.CONTINUE
					}
				})
				else {
					val pathInZipFile = zipfs.getPath("/", realPath.toString.substring(REPOSITORY_BASE_PATH.length + paramDest.length))
					val pathInZipFolder = pathInZipFile.getParent
					if (!Files.isDirectory(pathInZipFolder)) {
						Files.createDirectories(pathInZipFolder)
					}
					Logger.debug(s"compress: '$pathInZipFile'")
					Files.copy(realPath, pathInZipFile, StandardCopyOption.REPLACE_EXISTING)
				}
			})
			zipped = true
		} finally {
			if (!zipped) Files.deleteIfExists(zip)
			if (zipfs != null) zipfs.close()
		}

		success()
	} catch {
		case e: IOException =>
			Logger.error("compress:" + e.getMessage, e)
			error(e.getClass.getSimpleName + ":" + e.getMessage)
	}

	/*private def extract(params: JsonObject) = {
		var genFolder = false
		val dest = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("destination"))
		val folder = dest.resolve(params.getAsString("folderName"))
		try {
			if (!Files.isDirectory(folder)) {
				genFolder = true
				Files.createDirectories(folder)
			}
			val zip = params.getAsString("item")
			val env = new util.HashMap[String, String]
			env.put("create", "false")

			val zipfs = FileSystems.newFileSystem(URI.create("jar:file:" + Paths.get(REPOSITORY_BASE_PATH, zip).toString), env)
			try {
				Files.walkFileTree(zipfs.getPath("/"), new SimpleFileVisitor[Path]() {
					override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
						if (file.getNameCount > 0) {
							val dest = folder.resolve(if (file.getNameCount < 1) ""
							else file.subpath(0, file.getNameCount).toString)
							Logger.debug(s"extract $file to $dest")
							try {
								Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING)
							} catch {
								case ex: Exception =>
									Logger.error(ex.getMessage, ex)
							}
						}
						FileVisitResult.CONTINUE
					}

					@throws[IOException]
					override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
						val subFolder = folder.resolve(if (dir.getNameCount < 1) ""
						else dir.subpath(0, dir.getNameCount).toString)
						if (!Files.exists(subFolder)) Files.createDirectories(subFolder)
						FileVisitResult.CONTINUE
					}
				})
			} finally {
				if (zipfs != null) zipfs.close()
			}

			success()
		} catch {
			case e: IOException =>
				if (genFolder) FileUtils.deleteQuietly(folder.toFile)
				Logger.error("extract:" + e.getMessage, e)
				error(e.getMessage)
		}
	}*/

	@throws[IOException]
	private def getPermissions(path: Path) = {
		if (SystemUtils.IS_OS_WINDOWS) {
			"rw-r--r--"
		} else {
			val fileAttributeView = Files.getFileAttributeView(path, classOf[PosixFileAttributeView])
			val readAttributes = fileAttributeView.readAttributes
			val permissions = readAttributes.permissions
			PosixFilePermissions.toString(permissions)
		}
	}

	/**
		* http://www.programcreek.com/java-api-examples/index.php?api=java.nio.file.attribute.PosixFileAttributes
		*/
	/*@throws[IOException]
	private def setPermissions(file: File, permsCode: String, recursive: Boolean): String = {
		val fileAttributeView = Files.getFileAttributeView(file.toPath, classOf[PosixFileAttributeView])
		fileAttributeView.setPermissions(PosixFilePermissions.fromString(permsCode))
		if (file.isDirectory && recursive && file.listFiles != null) for (f <- file.listFiles) {
			setPermissions(f, permsCode, recursive)
		}
		permsCode
	}*/

	private def write(inputStream: InputStream, path: Path) = {
		try {
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
			true
		} catch {
			case ex: IOException =>
				Logger.error(ex.getMessage, ex)
				false
		}
	}

	/**
		* { "result": { "success": false, "error": "msg" } }
		*/
	private def error(msg: String): JsonObject = {
		val result = new JsonObject
		result.add("success", JsonPrimitive(java.lang.Boolean.FALSE))
		result.add("error", JsonPrimitive(msg))
		val json = new JsonObject
		json.add("result", result)
		json
	}

	/**
		* { "result": { "success": true, "error": null } }
		*/
	private def success(): JsonObject = {
		val result = new JsonObject
		result.add("success", JsonPrimitive(java.lang.Boolean.TRUE))
		result.add("error", null)
		val json = new JsonObject
		json.add("result", result)
		json
	}

	@throws[IOException]
	private def setError(t: Throwable, response: HttpServletResponse): Unit = {
		try { // { "result": { "success": false, "error": "message" } }
			val responseJsonObject = error(t.getMessage)
			response.setContentType(JSON_RESPONSE_TYPE)
			val out = response.getWriter
			out.print(responseJsonObject)
			out.flush()
		} catch {
			case ex: IOException => response.sendError(500, ex.getMessage)
		}
	}

	private val IMG_SUFFIXES = Seq(".png", ".jpg", ".jpeg", ".gif")

	def getFileType(file: Path): FileType = {
		if (Files.isDirectory(file)) {
			FileType.FOLDER
		} else {
			val isImg = IMG_SUFFIXES.find(s => file.getFileName.toString.toLowerCase.endsWith(s))
			isImg match {
				case Some(_) => FileType.IMAGE
				case None => FileType.FILE
			}
		}
	}


}
