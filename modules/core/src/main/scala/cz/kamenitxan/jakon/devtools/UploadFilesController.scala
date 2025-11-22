package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import io.javalin.http.Context
import org.apache.commons.io.IOUtils

import java.io.{File, FileInputStream}
import scala.language.postfixOps

/**
  * Fallback for missing nginx upload directory configuration in debug mode
  * Created by TPa on 07.07.18.
  */
class UploadFilesController  {

	def doGet(ctx: Context): String = {
		val fileName = ctx.path().replace("/upload", FileManagerController.REPOSITORY_BASE_PATH + "/basePath")
		val file = new File(fileName)
		if (file.exists()) {
			ctx.status(200)

			val suffix = {
				val lastDot = fileName.lastIndexOf('.')
				if (lastDot > 0) {
					fileName.substring(lastDot + 1)
				} else {
					""
				}
			}
			val contentType = suffixToContentType(suffix)
			ctx.header("Content-Type", contentType)

			val os = ctx.res().getOutputStream
			val is = new FileInputStream(file)

			IOUtils.copy(is, os)
			os.close()
			is.close()
			ctx.header("Content-Length", file.length().toString)
		}
		ctx.body()
	}

	private def suffixToContentType(suffix: String): String = {
		suffix.toLowerCase match {
			// Images
			case "png" => "image/png"
			case "jpg" => "image/jpg"
			case "jpeg" => "image/jpeg"
			case "gif" => "image/gif"
			case "bmp" => "image/bmp"
			case "svg" => "image/svg+xml"
			case "webp" => "image/webp"
			case "ico" => "image/x-icon"
			case "tif" | "tiff" => "image/tiff"

			// Text/HTML/CSS/JS
			case "html" => "text/html"
			case "htm" => "text/html"
			case "css" => "text/css"
			case "js" => "application/javascript"
			case "json" => "application/json"
			case "xml" => "application/xml"
			case "txt" => "text/plain"
			case "csv" => "text/csv"

			// Documents
			case "pdf" => "application/pdf"
			case "doc" => "application/msword"
			case "docx" => "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
			case "xls" => "application/vnd.ms-excel"
			case "xlsx" => "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
			case "ppt" => "application/vnd.ms-powerpoint"
			case "pptx" => "application/vnd.openxmlformats-officedocument.presentationml.presentation"
			case "odt" => "application/vnd.oasis.opendocument.text"
			case "ods" => "application/vnd.oasis.opendocument.spreadsheet"

			// Archives
			case "zip" => "application/zip"
			case "rar" => "application/vnd.rar"
			case "7z" => "application/x-7z-compressed"
			case "tar" => "application/x-tar"
			case "gz" => "application/gzip"

			// Audio
			case "mp3" => "audio/mpeg"
			case "wav" => "audio/wav"
			case "ogg" => "audio/ogg"
			case "aac" => "audio/aac"
			case "weba" => "audio/webm"

			// Video
			case "mp4" => "video/mp4"
			case "mpeg" => "video/mpeg"
			case "webm" => "video/webm"
			case "avi" => "video/x-msvideo"
			case "mov" => "video/quicktime"

			// Fonts
			case "woff" => "font/woff"
			case "woff2" => "font/woff2"
			case "ttf" => "font/ttf"
			case "otf" => "font/otf"

			case _ => "application/octet-stream"
		}
	}

}
