package core.pagelet

import com.google.gson.Gson
import core.pagelet.entity.GetResponse
import cz.kamenitxan.jakon.core.dynamic.JsonPageletInitializer
import cz.kamenitxan.jakon.core.dynamic.entity.{JsonErrorResponse, JsonFailResponse, ResponseStatus}
import okhttp3.{MediaType, OkHttpClient, Request, RequestBody}
import org.scalatest.DoNotDiscover
import test.JsonHelper._
import test.TestBase

/**
 * Created by TPa on 13.04.2020.
 */
@DoNotDiscover
class JsonPageletTest extends TestBase {

	private val gson = new Gson()
	private val prefix = "/jsonPagelet/"
	private val httpClient = new OkHttpClient()
	private val JsonType: MediaType = MediaType.get("application/json; charset=utf-8")

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

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonFailResponse])

		assert(resp.status == ResponseStatus.fail)
		assert(resp.data == "some_message")
	}

	test("example json pagelet - get throw") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + s"${prefix}throw"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[JsonErrorResponse])

		assert(resp.status == ResponseStatus.error)
		assert(resp.message.contains("IllegalAccessException"))
	}

	test("example json pagelet - get withDataAndConnection") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val message = "test_message"

		val url = host + s"${prefix}withDataAndConnection?msg=${message}"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[GetResponse])

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == s"${message}")
	}

	test("example json pagelet - post") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = host + s"${prefix}post"

		val request = new Request.Builder()
		  .url(url)
  		  .post(RequestBody.create(JsonType, ""))
		  .build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - post no validation") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = host + s"${prefix}postNoValidation"

		val request = new Request.Builder()
		  .url(url)
		  .post(RequestBody.create(JsonType, ""))
		  .build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - post throw") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = host + s"${prefix}postThrow"

		val request = new Request.Builder()
		  .url(url)
		  .post(RequestBody.create(JsonType, ""))
		  .build()

		val resp = getResponse(request, classOf[JsonErrorResponse])

		assert(resp.status == ResponseStatus.error)
		assert(resp.message.contains("IllegalAccessException"))
	}

	test("example json pagelet - post withDataAndConnection") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val message = "test_message"
		val url = host + s"${prefix}postWithDataAndConnection"

		val json = JSON("msg" -> message).toString
		val body = RequestBody.create(JsonType, json)

		val request = new Request.Builder()
		  .url(url)
		  .post(body)
		  .build()

		val resp = getResponse(request)

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == s"${message}")
	}

	test("example json pagelet - post validate") { _ =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))
		val url = host + s"${prefix}postValidate"

		val json = JSON("msg" -> "").toString
		val body = RequestBody.create(JsonType, json)

		val request = new Request.Builder()
		  .url(url)
		  .post(body)
		  .build()

		val resp = getResponse(request, classOf[JsonFailResponse])

		assert(resp.status == ResponseStatus.fail)
		assert(resp.data.toString.contains("test_json_pagelet"))
	}


	private def  getResponse[T](request: Request, responseType: Class[T] = classOf[GetResponse]): T = {
		val response = httpClient.newCall(request).execute
		val result = try {
			response.body.string
		} finally {
			response.close()
		}
		gson.fromJson(result, responseType)
	}

}
