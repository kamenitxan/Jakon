package webui

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import test.TestBase

import scala.io.Source

class FileManagerTest extends TestBase {
	val prefix = "/admin/files/"


	test("file manager - create folder") { f =>
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

	test("file manager - list") { f =>
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

	test("file manager - rename") { f =>
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

	test("file manager - list renamed") { f =>
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

	test("file manager - remove") { f =>
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

	test("file manager - list removed") { f =>
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
}
