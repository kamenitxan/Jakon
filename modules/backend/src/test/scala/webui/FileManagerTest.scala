package webui

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import org.apache.commons.lang3.SystemUtils
import test.TestBase

import scala.io.Source

class FileManagerTest extends TestBase {
	val prefix = "/admin/files/"


	test("file manager - create folder") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "createFolderUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"createFolder\",\"newPath\":\"/basePath/testFolder\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream)

		assert(resCode == 200)
		assert(res.nonEmpty) //testFolder
	}

	test("file manager - list") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "listUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"list\",\"path\":\"/basePath\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("testFolder"))
	}

	test("file manager - rename dir") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "rename")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"rename\",\"item\":\"/basePath/testFolder\",\"newItemPath\":\"/basePath/testFolder2\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
	}

	test("file manager - list renamed") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "listUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"list\",\"path\":\"/basePath\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("testFolder2"))
	}

	test("file manager - download nonexistent") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "downloadFileUrl?action=download&path=%2FbasePath%2FCzech-Republic-Flag.png")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		val resCode = con.getResponseCode
		// TODO: fix
		assert(resCode == 200 || resCode == 404)
	}

	test("file manager - download multiple nonexistent") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix
		  + "downloadMultipleFileUrl?action=downloadMultiple&toFilename=test.zip&items[]=%2FbasePath%2Fnope.png&items[]=%2FbasePath%2Fnope2.png")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		val resCode = con.getResponseCode
		// TODO: fix
		assert(resCode == 200 || resCode == 404)
	}


	val CR = 0x0D
	val LF = 0x0A

	test("file manager - upload") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "uploadUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------1778331513440480241804387961")
		con.setRequestProperty("Content-Lenght", "346")
		val fd = "-----------------------------1778331513440480241804387961\r\nContent-Disposition: form-data; name=\"destination\"\r\n\r\n/basePath\r\n-----------------------------1778331513440480241804387961\r\nContent-Disposition: form-data; name=\"file-0\"; filename=\"test.txt\"\r\nContent-Type: text/plain\r\n\r\ntestupload\r\n\r\n-----------------------------1778331513440480241804387961--\r\n"

		con.setDoInput(true)
		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)

		out.write(fd.getBytes())

		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - download multiple") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix
		  + "downloadMultipleFileUrl?action=downloadMultiple&toFilename=test.zip&items[]=%2FbasePath%2Ftest.txt&items[]=%2FbasePath%2Fnope2.png")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		val resCode = con.getResponseCode
		assert(resCode == 200)
	}


	test("file manager - rename file") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "renameUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"rename\",\"item\":\"/basePath/test.txt\",\"newItemPath\":\"/basePath/test2.txt\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - get content") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "getContentUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"getContent\",\"item\":\"/basePath/test2.txt\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("testupload"))
	}

	test("file manager - edit") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "editUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"edit",
			  |"content":"test edit",
			  |"item":"basePath/test2.txt"}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - move") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "editUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"move",
			  |"newPath":"/basePath/testFolder2",
			  |"items":["basePath/test2.txt"]}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - copy") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "copyUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"copy",
			  |"newPath":"/basePath/testFolder2/",
			  |"singleFilename":"test2-copy.txt",
			  |"items":["basePath/testFolder2/test2.txt"]}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - list moved + copied") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "listUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("""{"action":"list","path":"/basePath/testFolder2"}""".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("test2.txt"))
		assert(res.contains("test2-copy.txt"))
	}

	test("file manager - compress same name") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "compressUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"compress",
			  |"items":["/basePath/testFolder2"],
			  |"destination":"/basePath",
			  |"compressedFilename":"testFolder2"}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":false"))
	}

	test("file manager - compress") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "compressUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"compress",
			  |"items":["/basePath/testFolder2"],
			  |"destination":"/basePath",
			  |"compressedFilename":"testFolder2.zip"}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

	test("file manager - delete file") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "getContentUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"remove\",\"items\":[\"/basePath/testFolder2/test2.txt\", \"/basePath/testFolder2.zip\"]}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}


	test("file manager - remove folder") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "remove")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"remove\",\"items\":[\"/basePath/testFolder2\"]}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
	}

	test("file manager - list removed") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "listUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("GET")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")

		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"list\",\"path\":\"/basePath\"}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(!res.contains("testFolder2"))
	}

	test("file manager - change permission") { _ =>
		assume(!SystemUtils.IS_OS_WINDOWS)
		val url = new URL(host + prefix + "permissionsUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write(
			"""{"action":"changePermissions",
			  |"perms":"rw-rw-r--",
			  |"permsCode":"664",
			  |"items":["/basePath/test2.txt"]}""".stripMargin.getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":false"))
	}

}
