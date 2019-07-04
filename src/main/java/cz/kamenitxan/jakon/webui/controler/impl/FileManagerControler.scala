package cz.kamenitxan.jakon.webui.controler.impl

import java.io.IOException
import java.nio.file.attribute.{BasicFileAttributes, PosixFileAttributeView, PosixFilePermissions}
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.regex.Pattern

import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controler.impl.FileManagerServlet.Mode
import javax.servlet.ServletException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import net.minidev.json.{JSONObject, JSONValue}
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.slf4j.{Logger, LoggerFactory}
import spark.{Request, Response}

import scala.annotation.switch
import scala.collection.JavaConversions._

object FileManagerControler {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	private val REPOSITORY_BASE_PATH = "upload"
	private var DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z" // (Wed, 4 Jul 2001 12:08:56)
	private val enabledAction: util.Map[FileManagerServlet.Mode, Boolean] = new util.HashMap[FileManagerServlet.Mode, Boolean]

	init()

	def getManager(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/fileManager")
	}

	def getManagerFrame(req: Request, res: Response) = {
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
				if (isSupportFeature(Mode.upload)) {
					fm.uploadFile(req.raw(), res.raw())
				} else {
					setError(new IllegalAccessError(notSupportFeature(Mode.upload).getAsString("error")), res.raw())
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
		enabledAction.put(Mode.rename, enabledActions.contains("rename"))
		enabledAction.put(Mode.move, movePattern.matcher(enabledActions).find)
		enabledAction.put(Mode.remove, enabledActions.contains("remove"))
		enabledAction.put(Mode.edit, enabledActions.contains("edit"))
		enabledAction.put(Mode.createFolder, enabledActions.contains("createfolder"))
		enabledAction.put(Mode.changePermissions, enabledActions.contains("changepermissions"))
		enabledAction.put(Mode.compress, enabledActions.contains("compress"))
		enabledAction.put(Mode.extract, enabledActions.contains("extract"))
		enabledAction.put(Mode.copy, enabledActions.contains("copy"))
		enabledAction.put(Mode.upload, enabledActions.contains("upload"))
	}


	@throws[IOException]
	private def fileOperation(request: HttpServletRequest, response: HttpServletResponse, fm: FileManagerServlet): Unit = {
		var responseJsonObject: JSONObject = null
		try {
			val br = request.getReader
			val str = Stream.continually(br.readLine()).takeWhile(_ != null).mkString("\n")
			br.close()

			val params: JSONObject = JSONValue.parse(str, classOf[JSONObject])
			val mode: Mode = Mode.valueOf(params.getAsString("action"))
			responseJsonObject = (mode: @switch) match {
				case Mode.createFolder =>
					executeIfSupported(mode, params, p => createFolder(p))
				case Mode.changePermissions =>
					executeIfSupported(mode, params, p => fm.changePermissions(p))
				case Mode.compress =>
					executeIfSupported(mode, params, p => fm.compress(p))
				case Mode.copy =>
					executeIfSupported(mode, params, p => fm.copy(p))
				case Mode.remove =>
					executeIfSupported(mode, params, p => fm.remove(p))
				case Mode.getContent =>
					fm.getContent(params)
				case Mode.edit => // get content
					executeIfSupported(mode, params, p => fm.editFile(p))
				case Mode.extract =>
					executeIfSupported(mode, params, p => fm.extract(p))
				case Mode.list =>
					list(params)
				case Mode.rename =>
					executeIfSupported(mode, params, p => fm.rename(p))
				case Mode.move =>
					executeIfSupported(mode, params, p => fm.move(p))
				case _ =>
					throw new ServletException("not implemented")
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

	private def executeIfSupported(mode: Mode, params: JSONObject, fun: JSONObject => JSONObject): JSONObject = {
		if (isSupportFeature(mode)) {
			fun(params)
		} else {
			notSupportFeature(mode)
		}
	}

	private def isSupportFeature(mode: FileManagerServlet.Mode) = {
		logger.debug("check support {}", mode)
		enabledAction.get(mode)
	}

	private def notSupportFeature(mode: FileManagerServlet.Mode): JSONObject = error("This implementation not support " + mode + " feature")

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
		logger.debug("createFolder path: {} name: {}", path)
		Files.createDirectories(path)
		success(params)
	} catch {
		case _: FileAlreadyExistsException =>
			success(params)
		case e: IOException =>
			logger.error("createFolder:" + e.getMessage, e)
			error(e.getMessage)
	}

	@throws[IOException]
	private def getPermissions(path: Path) = {
		val fileAttributeView = Files.getFileAttributeView(path, classOf[PosixFileAttributeView])
		val readAttributes = fileAttributeView.readAttributes
		val permissions = readAttributes.permissions
		PosixFilePermissions.toString(permissions)
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
	private def success(params: JSONObject) = {
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
