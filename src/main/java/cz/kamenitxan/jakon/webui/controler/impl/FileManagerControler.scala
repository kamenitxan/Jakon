package cz.kamenitxan.jakon.webui.controler.impl

import java.io.{File, IOException}
import java.nio.file.attribute.{BasicFileAttributes, PosixFileAttributeView, PosixFilePermissions}
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths, StandardCopyOption}
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.regex.Pattern

import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.entity.FileManagerMode
import javax.servlet.ServletException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import net.minidev.json.{JSONArray, JSONObject, JSONValue}
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.FileUtils
import org.slf4j.{Logger, LoggerFactory}
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
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	private val REPOSITORY_BASE_PATH = "upload"
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
		val fm = new FileManagerServlet
		fm.init()
		fm.doGet(req.raw(), res.raw())
		res
	}

	def executePost(req: Request, res: Response): Response = {
		val fm = new FileManagerServlet
		fm.init()
		/*fm.doPost(req.raw(), res.raw())*/


		try { // if request contains multipart-form-data
			if (ServletFileUpload.isMultipartContent(req.raw())) {
				if (isSupportFeature(FileManagerMode.upload)) {
					fm.uploadFile(req.raw(), res.raw())
				} else {
					setError(new IllegalAccessError(notSupportFeature(FileManagerMode.upload).getAsString("error")), res.raw())
				}
			} else { // all other post request has jspn params in body}
				fileOperation(req.raw(), res.raw(), fm)
			}
		} catch {
			case ex@(_: ServletException | _: IOException) =>
				logger.error(ex.getMessage, ex)
				setError(ex, res.raw())
		}
		res
	}

	def init(): Unit = {
		val enabledActions = "createfolder, rename, remove, upload"
		val movePattern = Pattern.compile("\\bmove\\b")
		enabledAction.put(FileManagerMode.rename, enabledActions.contains("rename"))
		enabledAction.put(FileManagerMode.move, movePattern.matcher(enabledActions).find)
		enabledAction.put(FileManagerMode.remove, enabledActions.contains("remove"))
		enabledAction.put(FileManagerMode.edit, enabledActions.contains("edit"))
		enabledAction.put(FileManagerMode.createFolder, enabledActions.contains("createfolder"))
		enabledAction.put(FileManagerMode.changePermissions, enabledActions.contains("changepermissions"))
		enabledAction.put(FileManagerMode.compress, enabledActions.contains("compress"))
		enabledAction.put(FileManagerMode.extract, enabledActions.contains("extract"))
		enabledAction.put(FileManagerMode.copy, enabledActions.contains("copy"))
		enabledAction.put(FileManagerMode.upload, enabledActions.contains("upload"))
	}


	@throws[IOException]
	private def fileOperation(request: HttpServletRequest, response: HttpServletResponse, fm: FileManagerServlet): Unit = {
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
					executeIfSupported(mode, params, p => changePermissions(p))
				case FileManagerMode.compress =>
					executeIfSupported(mode, params, p => fm.compress(p))
				case FileManagerMode.copy =>
					executeIfSupported(mode, params, p => copy(p))
				case FileManagerMode.remove =>
					executeIfSupported(mode, params, p => remove(p))
				case FileManagerMode.getContent =>
					getContent(params)
				case FileManagerMode.edit => // get content
					executeIfSupported(mode, params, p => editFile(p))
				case FileManagerMode.extract =>
					executeIfSupported(mode, params, p => fm.extract(p))
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
		logger.debug("check support {}", mode)
		enabledAction.get(mode)
	}

	private def notSupportFeature(mode: FileManagerMode): JSONObject = error("This implementation not support " + mode + " feature")

	private def list(params: JSONObject) = {
		try {
			val onlyFolders = "true".equalsIgnoreCase(params.getAsString("onlyFolders"))
			val path = params.getAsString("path")
			logger.debug(s"list path: Paths.get('$REPOSITORY_BASE_PATH', '$path'), onlyFolders: $onlyFolders")
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
				case ex: IOException => logger.error("Error while listing files", ex)
			} finally {
				if (directoryStream != null) directoryStream.close()
			}

			val json = new JSONObject
			json.put("result", resultList)
			json
		} catch {
			case e: Exception =>
				logger.error("list:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def createFolder(params: JSONObject) = try {
		val path = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("newPath"))
		logger.debug(s"createFolder path: $path")
		Files.createDirectories(path)
		success()
	} catch {
		case _: FileAlreadyExistsException =>
			success()
		case e: IOException =>
			logger.error("createFolder:" + e.getMessage, e)
			error(e.getMessage)
	}

	private def changePermissions(params: JSONObject) = {
		try {
			val paths = params.get("items").asInstanceOf[JSONArray]
			val perms = params.getAsString("perms") // "rw-r-x-wx"
			val permsCode = params.getAsString("permsCode") // "653"
			val recursive = "true".equalsIgnoreCase(params.getAsString("recursive"))
			for (path <- paths) {
				logger.debug(s"changepermissions path: $path, perms: $perms, permsCode: $permsCode, recursive: $recursive")
				val f = Paths.get(REPOSITORY_BASE_PATH, path.toString).toFile
				setPermissions(f, perms, recursive)
			}
			success()
		} catch {
			case e: IOException =>
				logger.error("changepermissions:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

	private def move(params: JSONObject): JSONObject = try { //TODO: minidev json should be rewrited to gson
		val paths = params.get("items").asInstanceOf[JSONArray]
		val newpath = Paths.get(REPOSITORY_BASE_PATH, params.getAsString("newPath"))
		for (obj <- paths) {
			val path = Paths.get(REPOSITORY_BASE_PATH, obj.toString)
			val mpath = newpath.resolve(path.getFileName)
			logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
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
			logger.error("move:" + e.getMessage, e)
			error(e.getMessage)
	}

	private def rename(params: JSONObject) = {
		try {
			val path = params.getAsString("item")
			val newpath = params.getAsString("newItemPath")
			logger.debug(s"rename from: $path to: $newpath")
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
				logger.error("rename:" + e.getMessage, e)
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
				logger.debug("remove {}", path)
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
				logger.error("getContent:" + ex.getMessage, ex)
				error(ex.getMessage)
		}
	}

	private def editFile(params: JSONObject) = { // get content
		try {
			val path = params.getAsString("item")
			logger.debug(s"editFile path: $path")
			val srcFile = new File(REPOSITORY_BASE_PATH, path)
			val content = params.getAsString("content")
			FileUtils.writeStringToFile(srcFile, content)
			success()
		} catch {
			case e: IOException =>
				logger.error("editFile:" + e.getMessage, e)
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
				logger.debug(s"mv $path to $mpath exists? ${Files.exists(mpath)}")
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
				logger.error("copy:" + e.getMessage, e)
				error(e.getMessage)
		}
	}

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
	@throws[IOException]
	private def setPermissions(file: File, permsCode: String, recursive: Boolean): String = {
		val fileAttributeView = Files.getFileAttributeView(file.toPath, classOf[PosixFileAttributeView])
		fileAttributeView.setPermissions(PosixFilePermissions.fromString(permsCode))
		if (file.isDirectory && recursive && file.listFiles != null) for (f <- file.listFiles) {
			setPermissions(f, permsCode, recursive)
		}
		permsCode
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
