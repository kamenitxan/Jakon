package core.pagelet

import com.google.gson.Gson
import core.pagelet.entity.GetResponse
import cz.kamenitxan.jakon.core.dynamic.JsonPageletInitializer
import cz.kamenitxan.jakon.core.dynamic.entity.{JsonErrorResponse, JsonFailResponse, ResponseStatus}
import org.scalatest.DoNotDiscover
import test.JsonHelper.*
import test.TestBase

import java.net.URI
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

/**
 * Created by TPa on 13.04.2020.
 */
@DoNotDiscover
class JsonPageletTest extends TestBase {

	private val gson = new Gson()
	private val prefix = "/jsonPagelet/"
	private val httpClient = HttpClient.newHttpClient()
	private val jsonContentType = "application/json;charset=utf-8"

	test("example json pagelet - get") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + s"${prefix}get"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[GetResponse])

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - get response") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + s"${prefix}getResponse"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonFailResponse[String]])

		assert(resp.status == ResponseStatus.fail)
		assert(resp.data == "some_message")
	}

	test("example json pagelet - get throw") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + s"${prefix}throw"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonErrorResponse[AnyRef]])

		assert(resp.status == ResponseStatus.error)
		assert(resp.message.contains("IllegalAccessException"))
	}

	test("example json pagelet - get withDataAndConnection") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val message = "test_message"

		val url = host + s"${prefix}withDataAndConnection?msg=$message"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[GetResponse])

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == s"$message")
	}

	test("example json pagelet - post") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = new URI(host + s"${prefix}post")

		val request = HttpRequest.newBuilder()
			.uri(url)
			.headers("Content-Type", jsonContentType)
			.POST(HttpRequest.BodyPublishers.ofString(""))
			.build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - post no validation") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = new URI(host + s"${prefix}postNoValidation")

		val request = HttpRequest.newBuilder()
			.uri(url)
			.headers("Content-Type", jsonContentType)
			.POST(HttpRequest.BodyPublishers.ofString(""))
			.build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - post throw") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = new URI(host + s"${prefix}postThrow")

		val request = HttpRequest.newBuilder()
			.uri(url)
			.headers("Content-Type", jsonContentType)
			.POST(HttpRequest.BodyPublishers.ofString(""))
			.build()

		val resp = getResponse(request, classOf[JsonErrorResponse[AnyRef]])

		assert(resp.status == ResponseStatus.error)
		assert(resp.message.contains("IllegalAccessException"))
	}

	test("example json pagelet - post withDataAndConnection") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val message = "test_message"
		val url = new URI(host + s"${prefix}postWithDataAndConnection")

		val json = JSON("msg" -> message).toString

		val request = HttpRequest.newBuilder()
			.uri(url)
			.headers("Content-Type", jsonContentType)
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == s"$message")
	}

	test("example json pagelet - post validate") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = new URI(host + s"${prefix}postValidate")

		val json = JSON("msg" -> "").toString

		val request = HttpRequest.newBuilder()
			.uri(url)
			.headers("Content-Type", jsonContentType)
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build()

		val resp = getResponse(request, classOf[JsonFailResponse[AnyRef]])

		assert(resp.status == ResponseStatus.fail)
		assert(resp.data.toString.contains("test_json_pagelet"))
	}


	private def getResponse[T](request: HttpRequest, responseType: Class[T] = classOf[GetResponse]): T = {
		val response: HttpResponse[String] = httpClient.send(request, BodyHandlers.ofString)
		gson.fromJson(response.body(), responseType)
	}

}
