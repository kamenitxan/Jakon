package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.core.configuration.Settings
import org.scalatest.funsuite.AnyFunSuite

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{CookieManager, CookiePolicy, URI, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.jdk.OptionConverters.*

/** Base for tests calling the running shop over HTTP. */
abstract class ShopHttpTestBase extends AnyFunSuite with ShopDbSupport {

	protected def host: String = s"http://127.0.0.1:${Settings.getPort}"

	/** HTTP client with its own cookie jar - behaves like a single browser session. Redirects are not followed. */
	class Browser {
		private val cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL)
		private val client = HttpClient.newBuilder()
			.cookieHandler(cookies)
			.followRedirects(HttpClient.Redirect.NEVER)
			.build()

		def get(path: String): HttpResponse[String] = {
			val request = HttpRequest.newBuilder(URI.create(host + path)).GET().build()
			client.send(request, HttpResponse.BodyHandlers.ofString())
		}

		def post(path: String, params: (String, String)*): HttpResponse[String] = {
			val body = params.map((k, v) => encode(k) + "=" + encode(v)).mkString("&")
			val request = HttpRequest.newBuilder(URI.create(host + path))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build()
			client.send(request, HttpResponse.BodyHandlers.ofString())
		}

		def cookie(name: String): Option[String] = {
			import scala.jdk.CollectionConverters.*
			cookies.getCookieStore.getCookies.asScala.find(_.getName == name).map(_.getValue)
		}

		private def encode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)
	}

	protected def location(resp: HttpResponse[String]): String = {
		resp.headers().firstValue("Location").toScala.getOrElse("")
	}

	protected def assertRedirect(resp: HttpResponse[String], target: String): Unit = {
		assert(resp.statusCode() == 302, s"expected redirect, got ${resp.statusCode()}: ${resp.body().take(500)}")
		assert(location(resp).startsWith(target), s"expected redirect to $target, got ${location(resp)}")
	}

	protected def assertOk(resp: HttpResponse[String]): Unit = {
		assert(resp.statusCode() == 200, s"expected 200, got ${resp.statusCode()}: ${resp.body().take(500)}")
	}
}
