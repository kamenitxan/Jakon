package jakontest.devtools

import cz.kamenitxan.jakon.devtools.SafeFileResolver
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import jakontest.test.TestBase
import org.openqa.selenium.WebDriver
import org.scalatest.DoNotDiscover

import jakontest.test.HttpBrowser

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files

/**
 * Created by TPa on 04.04.2020.
 */
@DoNotDiscover
class DevtoolsTest extends TestBase {


	test("UploadFilesController test") { f =>
		val content = "test"
		val file = new File(FileManagerController.REPOSITORY_BASE_PATH + "/basePath/ufctest.txt")
		file.createNewFile()

		val fw = new FileWriter(file.getAbsoluteFile)
		val bw = new BufferedWriter(fw)
		bw.write(content)
		bw.close()
		fw.close()

		implicit val driver: WebDriver = f.driver
		driver.get(host + "/upload/ufctest.txt")
		assert(driver.getPageSource.contains(content))
	}

	test("UploadFilesController sets content type by file suffix") { _ =>
		val expected = Map(
			"png" -> "image/png", "jpg" -> "image/jpg", "jpeg" -> "image/jpeg", "gif" -> "image/gif", "bmp" -> "image/bmp",
			"svg" -> "image/svg+xml", "webp" -> "image/webp", "ico" -> "image/x-icon", "tif" -> "image/tiff", "tiff" -> "image/tiff",
			"html" -> "text/html", "htm" -> "text/html", "css" -> "text/css", "js" -> "application/javascript",
			"json" -> "application/json", "xml" -> "application/xml", "txt" -> "text/plain", "csv" -> "text/csv",
			"pdf" -> "application/pdf", "doc" -> "application/msword",
			"docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"xls" -> "application/vnd.ms-excel", "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"ppt" -> "application/vnd.ms-powerpoint",
			"pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"odt" -> "application/vnd.oasis.opendocument.text", "ods" -> "application/vnd.oasis.opendocument.spreadsheet",
			"zip" -> "application/zip", "rar" -> "application/vnd.rar", "7z" -> "application/x-7z-compressed",
			"tar" -> "application/x-tar", "gz" -> "application/gzip",
			"mp3" -> "audio/mpeg", "wav" -> "audio/wav", "ogg" -> "audio/ogg", "aac" -> "audio/aac", "weba" -> "audio/webm",
			"mp4" -> "video/mp4", "mpeg" -> "video/mpeg", "webm" -> "video/webm", "avi" -> "video/x-msvideo", "mov" -> "video/quicktime",
			"woff" -> "font/woff", "woff2" -> "font/woff2", "ttf" -> "font/ttf", "otf" -> "font/otf",
			"unknownext" -> "application/octet-stream"
		)
		val dir = new File(FileManagerController.REPOSITORY_BASE_PATH + "/basePath/contentTypes")
		dir.mkdirs()
		val browser = new HttpBrowser
		expected.foreach((suffix, contentType) => {
			val file = new File(dir, s"file.$suffix")
			Files.writeString(file.toPath, "content")
			val resp = browser.get(s"/upload/contentTypes/file.$suffix")
			assert(resp.statusCode() == 200, suffix)
			assert(resp.headers().firstValue("Content-Type").orElse("").startsWith(contentType), suffix)
		})

		// file without suffix
		Files.writeString(new File(dir, "noSuffix").toPath, "content")
		assert(browser.get("/upload/contentTypes/noSuffix").headers().firstValue("Content-Type").orElse("").startsWith("application/octet-stream"))
	}

	test("SafeFileResolver prevents path traversal") { _ =>
		val base = FileManagerController.REPOSITORY_BASE_PATH + "/basePath"
		val file = new File(base + "/sfrtest.txt")
		file.createNewFile()

		assert(SafeFileResolver.resolve(base, "/sfrtest.txt").isDefined)
		assert(SafeFileResolver.resolve(base, "/../basePath/sfrtest.txt").isDefined)
		assert(SafeFileResolver.resolve(base, "/../../build.sbt").isEmpty)
		assert(SafeFileResolver.resolve(base, "/nonexistent.txt").isEmpty)
		assert(SafeFileResolver.resolve(base, "/").isEmpty)
	}



}
