package webui

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import test.TestBase

import scala.io.Source

class FileManagerTest extends TestBase {
	val prefix = "/admin/files/"


	test("file manager - create folder") { _ =>
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

	test("file manager - remove") { _ =>
		val url = new URL(host + prefix + "rename")
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

	test("file manager - download nonexistent") { _ =>
		val url = new URL(host + prefix + "downloadFileUrl?action=download&path=%2FbasePath%2FCzech-Republic-Flag.png")
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


	test("file manager - rename file") { _ =>
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

	test("file manager - get countent") { _ =>
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

	test("file manager - delete") { _ =>
		val url = new URL(host + prefix + "getContentUrl")
		val con = url.openConnection.asInstanceOf[HttpURLConnection]
		con.setRequestMethod("POST")
		con.setRequestProperty("Content-Type", "application/json;charset=utf-8")


		con.setDoOutput(true)
		val out = new DataOutputStream(con.getOutputStream)
		out.write("{\"action\":\"remove\",\"items\":[\"/basePath/test2.txt\"]}".getBytes())
		out.flush()

		val resCode = con.getResponseCode
		val res = Source.fromInputStream(con.getInputStream).mkString

		assert(resCode == 200)
		assert(res.nonEmpty)
		assert(res.contains("\"success\":true"))
	}

}
