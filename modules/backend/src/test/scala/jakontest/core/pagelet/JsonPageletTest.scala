package jakontest.core.pagelet

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.dynamic.JsonPageletInitializer
import cz.kamenitxan.jakon.core.dynamic.entity.{JsonErrorResponse, JsonFailResponse, ResponseStatus}
import cz.kamenitxan.jakon.logging.Logger
import jakontest.core.pagelet.entity.GetResponse
import jakontest.test.JsonHelper.*
import jakontest.test.TestBase
import org.scalatest.DoNotDiscover

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

		val url = host + s"${prefix}getResponse"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonFailResponse[String]])

		assert(resp.status == ResponseStatus.fail)
		assert(resp.data == "some_message")
	}

	test("example json pagelet - get throw") { f =>

		val url = host + s"${prefix}throw"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonErrorResponse[AnyRef]])

		assert(resp.status == ResponseStatus.error)
		assert(resp.message.contains("IllegalAccessException"))
	}

	test("example json pagelet - get withDataAndConnection") { f =>
		val message = "test_message"

		val url = host + s"${prefix}withDataAndConnection?msg=$message"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[GetResponse])

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == s"$message")
	}

	test("example json pagelet - post") { _ =>
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

	test("example json pagelet - get typed response") { f =>

		val url = host + s"${prefix}getTyped"
		f.driver.get(url)

		val resp = f.driver.getPageSource

		Logger.error(resp)

		assert(resp.contains("\"msg\": \"msg\""))
		assert(resp.contains("\"num\": 1"))
		assert(resp.contains("a"))
		assert(resp.contains("b"))
		assert(resp.contains("18"))
		assert(resp.contains("19"))
		assert(resp.contains("\"optStr\": \"optStr\""))
		assert(resp.contains("\"optInt\": 75,"))
		assert(resp.contains("\"localDateTime\": \"2024-07-13T00:00\""))
		assert(resp.contains("\"zonedDateTimeData\": \"2024-07-13T00:00Z\""))
		assert(resp.contains("\"integer\": 1"))
		assert(resp.contains("key"))
		assert(resp.contains("value"))
		assert(!resp.contains("optStrEmpty"))
		assert(!resp.contains("integerNull"))

	}


	private def getResponse[T](request: HttpRequest, responseType: Class[T] = classOf[GetResponse]): T = {
		val response: HttpResponse[String] = httpClient.send(request, BodyHandlers.ofString)
		gson.fromJson(response.body(), responseType)
	}

}
