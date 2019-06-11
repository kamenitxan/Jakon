package webui

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import cz.kamenitxan.jakon.core.configuration.Settings
import org.scalatest.FunSuite

import scala.io.Source

class ApiTest extends FunSuite {

	var host = ""



	test("search") {
		val url = "http://localhost:"  + Settings.getPort + "/admin/api/search"
		val obj = new URL(url)
		val con = obj.openConnection.asInstanceOf[HttpURLConnection]


		con.setRequestMethod("POST")
		con.setDoOutput(true)
		val wr = new DataOutputStream(con.getOutputStream)
		wr.writeBytes("{\"objectName\":\"AclRule\",\"query\":\"\"}")
		wr.flush()

		val responseCode = con.getResponseCode
		val response= Source.fromInputStream(con.getInputStream).mkString

		//print result
		assert(responseCode == 200)
		assert(response.contains("AclRule(Admin)"))
	}

	test("search by id") {
		val url = "http://localhost:"  + Settings.getPort + "/admin/api/search"
		val obj = new URL(url)
		val con = obj.openConnection.asInstanceOf[HttpURLConnection]


		con.setRequestMethod("POST")
		con.setDoOutput(true)
		val wr = new DataOutputStream(con.getOutputStream)
		wr.writeBytes("{\"objectName\":\"AclRule\",\"query\":\"1\"}")
		wr.flush()

		val responseCode = con.getResponseCode
		val response= Source.fromInputStream(con.getInputStream).mkString

		//print result
		assert(responseCode == 200)
		assert(response.contains("AclRule(Admin)"))
	}

}
