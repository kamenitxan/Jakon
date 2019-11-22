package cz.kamenitxan.jakon.webui.controler.impl

import java.io._
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, PosixFileAttributeView, PosixFilePermissions}
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util
import java.util.Date
import java.util.zip.{ZipEntry, ZipOutputStream}

import cz.kamenitxan.jakon.core.model.{FileType, JakonFile}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.entity.FileManagerMode
import javax.mail.internet.MimeUtility
import javax.servlet.ServletException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import net.minidev.json.{JSONArray, JSONObject, JSONValue}
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.FileUtils
import spark.{Request, Response}

import scala.annotation.switch
import scala.collection.JavaConversions._

/**
  * This controler serve angular-filemanager call<br>
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
object FileManagerControler {
	val REPOSITORY_BASE_PATH = "upload"
	private var DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z" // (Wed, 4 Jul 2001 12:08:56)
	private val enabledAction: util.Map[FileManagerMode, Boolean] = new util.HashMap[FileManagerMode, Boolean]

	init()

	def getManager(req: Request, res: Response): Context = {
		new Context(Map[String, Any](), "objects/fileManager")
	}

	def getManagerFrame(req: Request, res: Response): Context = {
		new Context(Map[String, Any](), "objects/fileManagerFrame")
	}

	def executeGet(req: Request, res: Response): Response = {
		val request = req.raw()
		val response = res.raw()

		val action = request.getParameter("action")
		if ("download" == action) {
			val path = request.getParameter("path")
			val file = new File(REPOSITORY_BASE_PATH, path)
			if (!file.isFile) { // if not a file, it is a folder, show this error.
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found")
				return res
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

		res
	}

	def executePost(req: Request, res: Response): Response = {
		try { // if request contains multipart-form-data
			if (ServletFileUpload.isMultipartContent(req.raw())) {
				if (isSupportFeature(FileManagerMode.upload)) {
					uploadFile(req.raw(), res.raw())
				} else {
					setError(new IllegalAccessError(notSupportFeature(FileManagerMode.upload).getAsString("error")), res.raw())
				}
			} else { // all other post request has jspn params in body}
				fileOperation(req.raw(), res.raw())
			}
		} catch {
			case ex@(_: ServletException | _: IOException) =>
				Logger.error(ex.getMessage, ex)
				setError(ex, res.raw())
		}
		res
	}

	def init(): Unit = {
		enabledAction.put(FileManagerMode.rename, true)
		enabledAction.put(FileManagerMode.move, true)
		enabledAction.put(FileManagerMode.remove, true)
		enabledAction.put(FileManagerMode.edit, true)
		enabledAction.put(FileManagerMode.createFolder, true)
		enabledAction.put(FileManagerMode.changePermissions, false)
		enabledAction.put(FileManagerMode.compress, true)
		enabledAction.put(FileManagerMode.extract, false)
		enabledAction.put(FileManagerMode.copy, true)
		enabledAction.put(FileManagerMode.upload, true)
	}


	@throws[IOException]
	private def fileOperation(request: HttpServletRequest, response: HttpServletResponse): Unit = {
		var responseJsonObject: JSONObject = null
		try {
			val br = request.getReader
			val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
			br.close()

			val params: JSONObject = JSONValue.parse(str, classOf[JSONObject])
			val mode: FileManagerMode = FileManagerMode.valueOf(params.getAsString("action"))
			responseJsonObject = (mode: @switch) match {
				case FileManagerMode.createFolder =>
					executeIfSupported(mode, params, p => createFolder(p))
				case FileManagerMode.changePermissions =>
					executeIfSupported(mode, params, p => null) //changePermissions(p))
				case FileManagerMode.compress =>
					executeIfSupported(mode, params, p => compress(p))
				case FileManagerMode.copy =>
					executeIfSupported(mode, params, p => copy(p))
				case FileManagerMode.remove =>
					executeIfSupported(mode, params, p => remove(p))
				case FileManagerMode.getContent =>
					getContent(params)
				case FileManagerMode.edit => // get content
					executeIfSupported(mode, params, p => editFile(p))
				case FileManagerMode.extract =>
					executeIfSupported(mode, params, p => null) //extract(p))
				case FileManagerMode.list =>
					list(params)
				case FileManagerMode.rename =>
					executeIfSupported(mode, params, p => rename(p))
				case FileManagerMode.move =>
					executeIfSupported(mode, params, p => move(p))
				case _ =>
					throw new UnsupportedOperationException("not implemented")
			}
			if (responseJsonObject == null) responseJsonObject = error("generic error : responseJsonObject is null")
		} catch {
			case e@(_: IOException | _: ServletException) =>
				responseJsonObject = error(e.getMessage)
		}
		response.setContentType("application/json;charset=UTF-8")
		val out = response.getWriter
		out.print(responseJsonObject)
		out.flush()
	}

	private def executeIfSupported(mode: FileManagerMode, params: JSONObject, fun: JSONObject => JSONObject): JSONObject = {
		if (isSupportFeature(mode)) {
			fun(params)
		} else {
			notSupportFeature(mode)
		}
	}

	private def isSupportFeature(mode: FileManagerMode) = {
		Logger.debug(s"check support $mode")
		enabledAction.get(mode)
	}

	private def notSupportFeature(mode: FileManagerMode): JSONObject = error("This implementation not support " + mode + " feature")


	/**
	  * URL: $config.uploadUrl, Method: POST, Content-Type: multipart/form-data
	  * Unlimited file upload, each item will be enumerated as file-1, file-2, etc.
	  * [$config.uploadUrl]?destination=/public_html/image.jpg&file-1={..}&file-2={...}
	  */
	@throws[ServletException]
	private def uploadFile(request: HttpServletRequest, response: HttpServletResponse): Unit = {
		if (isSupportFeature(FileManagerMode.upload)) {
			Logger.debug("upload now")
			try {
				var destination: String = null
				val files = new util.HashMap[String, InputStream]
				val sfu = new ServletFileUpload(new DiskFileItemFactory)
				sfu.setHeaderEncoding("UTF-8")
				val items = sfu.parseRequest(request)
				for (item <- items) {
					if (item.isFormField) { // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
						if ("destination" == item.getFieldName) {
							destination = item.getString("UTF-8")
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
					for (fileEntry <- files.entrySet) {
						val path = Paths.get(REPOSITORY_BASE_PATH + destination, fileEntry.getKey)
						if (!write(fileEntry.getValue, path)) {
							Logger.debug("write error")
							throw new Exception("write error")
						}
					}
					var responseJsonObject: JSONObject = null
					responseJsonObject = this.success()
					response.setContentType("application/json;charset=UTF-8")
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
			throw new ServletException(notSupportFeature(FileManagerMode.upload).getAsString("error"))
		}
	}

	private def list(params: JSONObject) = {
		try {
			val onlyFolders = "true".equalsIgnoreCase(params.getAsString("onlyFolders"))
			val path = params.getAsString("path")
			Logger.debug(s"list path: Paths.get('$REPOSITORY_BASE_PATH', '$path'), onlyFolders: $onlyFolders")
			val resultList = new util.ArrayList[JSONObject]
			val directoryStream = Files.newDirectoryStream(Paths.get(REPOSITORY_BASE_PATH, path))
			try {
				val dt = new SimpleDateFormat(DATE_FORMAT)
				directoryStream.filterNot(p => {
					val attrs = Files.readAttributes(p, classOf[BasicFileAttributes])
					onlyFolders && !attrs.isDirectory
				}).foreach(p => {
					val attrs = Files.readAttributes(p, classOf[BasicFileAttributes])
					val el = new JSONObject
					el.put("name", p.getFileName.toString)
					el.put("rights", getPermissions(p))
					el.put("date", dt.format(new Date(attrs.lastModifiedTime.toMillis)))
					el.put("size", java.lang.Long.valueOf(attrs.size))
					el.put("type", if (attrs.isDirectory) "dir" else "file")
					resultList.add(el)
				})

			} catch {
				case ex: IOException => Logger.error("Error while listing files", ex)
			} finally {
				if (directoryStream != null) directoryStream.close()
			}

			val json = new JSONObject
			json.put("result", resultList)
			json
		} catch {
			case e: Exception =>
				Logger.error("list:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def createFolder(params: JSONObject) = try {
		val path = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("newPath"))
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

	/*private def changePermissions(params: JSONObject) = {
		try {
			val paths = params.get("items").asInstanceOf[JSONArray]
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

	private def move(params: JSONObject): JSONObject = try { //TODO: minidev json should be rewrited to gson
		val paths = params.get("items").asInstanceOf[JSONArray]
		val newpath = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("newPath"))
		for (obj <- paths) {
			val path = Paths.get(REPOSITORY_BASE_PATH, obj.toString)
			val mpath = newpath.resolve(path.getFileName)
			Logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
			if (Files.exists(mpath)) return error(mpath.toString + " already exits!")
		}
		for (obj <- paths) {
			val path = Paths.get(REPOSITORY_BASE_PATH, obj.toString)
			val mpath = newpath.resolve(path.getFileName)
			Files.move(path, mpath, StandardCopyOption.REPLACE_EXISTING)
		}
		success()
	} catch {
		case e: IOException =>
			Logger.error("move:" + e.getMessage, e)
			error(e.getMessage)
	}

	private def rename(params: JSONObject) = {
		try {
			val path = params.getAsString("item")
			val newpath = params.getAsString("newItemPath")
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

	@throws[ServletException]
	private def remove(params: JSONObject): JSONObject = {
		val paths = params.get("items").asInstanceOf[JSONArray]
		val error = new StringBuilder
		val sb = new StringBuilder
		for (obj <- paths) {
			val path = Paths.get(REPOSITORY_BASE_PATH, obj.toString)
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
		}
		if (error.nonEmpty) {
			if (sb.nonEmpty) {
				sb.append("\nPlease refresh this folder to list last result.")
			}
			throw new ServletException(error.toString + sb.toString)
		} else {
			success()
		}
	}

	private def getContent(params: JSONObject) = {
		try {
			val json = new JSONObject
			json.put("result", FileUtils.readFileToString(Paths.get(REPOSITORY_BASE_PATH, params.getAsString("item")).toFile))
			json
		} catch {
			case ex: IOException =>
				Logger.error("getContent:" + ex.getMessage, ex)
				error(ex.getMessage)
		}
	}

	private def editFile(params: JSONObject) = { // get content
		try {
			val path = params.getAsString("item")
			Logger.debug(s"editFile path: $path")
			val srcFile = new File(REPOSITORY_BASE_PATH, path)
			val content = params.getAsString("content")
			FileUtils.writeStringToFile(srcFile, content)
			success()
		} catch {
			case e: IOException =>
				Logger.error("editFile:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def copy(params: JSONObject): JSONObject = {
		try {
			val paths = params.get("items").asInstanceOf[JSONArray]
			val newpath = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("newPath"))
			val newFileName = params.getAsString("singleFilename")
			for (obj <- paths) {
				val path = if (newFileName == null) {
					Paths.get(REPOSITORY_BASE_PATH, obj.toString)
				} else {
					Paths.get(".", newFileName)
				}
				val mpath = newpath.resolve(path.getFileName)
				Logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
				if (Files.exists(mpath)) {
					return error(mpath.toString + " already exits!")
				}
			}
			for (obj <- paths) {
				val path = Paths.get(REPOSITORY_BASE_PATH, obj.toString)
				val mpath = newpath.resolve(if (newFileName == null) path.getFileName
				else Paths.get(".", newFileName).getFileName)
				Files.copy(path, mpath, StandardCopyOption.REPLACE_EXISTING)
			}
			success()
		} catch {
			case e: IOException =>
				Logger.error("copy:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def compress(params: JSONObject): JSONObject = try {
		val paths = params.get("items").asInstanceOf[JSONArray]
		val paramDest = params.getAsString("destination")
		val dest = Paths.get(REPOSITORY_BASE_PATH, paramDest)
		val zip = dest.resolve(params.getAsString("compressedFilename"))
		if (Files.exists(zip)) {
			return error(zip.toString + " already exits!")
		}
		val env = new util.HashMap[String, String]
		env.put("create", "true")
		var zipped = false

		val appDir = dest.toAbsolutePath.toString.replace("upload/basePath", "")
		val zipfs = FileSystems.newFileSystem(URI.create(s"jar:file:$appDir" + zip.toString), env)
		try {
			for (path <- paths) {
				val realPath = Paths.get(REPOSITORY_BASE_PATH, path.toString)
				if (Files.isDirectory(realPath)) Files.walkFileTree(Paths.get(REPOSITORY_BASE_PATH, path.toString), new SimpleFileVisitor[Path]() {
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
			}
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

	/*private def extract(params: JSONObject) = {
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
		val fileAttributeView = Files.getFileAttributeView(path, classOf[PosixFileAttributeView])
		val readAttributes = fileAttributeView.readAttributes
		val permissions = readAttributes.permissions
		PosixFilePermissions.toString(permissions)
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
	private def error(msg: String) = {
		val result = new JSONObject
		result.put("success", java.lang.Boolean.FALSE)
		result.put("error", msg)
		val json = new JSONObject
		json.put("result", result)
		json
	}

	/**
	  * { "result": { "success": true, "error": null } }
	  */
	private def success() = {
		val result = new JSONObject
		result.put("success", java.lang.Boolean.TRUE)
		result.put("error", null)
		val json = new JSONObject
		json.put("result", result)
		json
	}

	@throws[IOException]
	private def setError(t: Throwable, response: HttpServletResponse): Unit = {
		try { // { "result": { "success": false, "error": "message" } }
			val responseJsonObject = error(t.getMessage)
			response.setContentType("application/json;charset=UTF-8")
			val out = response.getWriter
			out.print(responseJsonObject)
			out.flush()
		} catch {
			case ex: IOException => response.sendError(500, ex.getMessage)
		}
	}
}
