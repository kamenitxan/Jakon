package jakontest.test

import cz.kamenitxan.jakon.core.configuration.Settings

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{CookieManager, CookiePolicy, URI, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.jdk.OptionConverters.*

/**
 * HTTP client with its own cookie jar - behaves like a single browser session.
 * Unlike HtmlUnit it can send arbitrary POST requests. Redirects are not followed.
 */
class HttpBrowser {
	private val cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL)
	private val client = HttpClient.newBuilder()
		.cookieHandler(cookies)
		.followRedirects(HttpClient.Redirect.NEVER)
		.build()

	def host: String = s"http://localhost:${Settings.getPort}"

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

	/** Logs in through admin login form. */
	def login(email: String, password: String): HttpResponse[String] = post("/admin", "email" -> email, "password" -> password)

	private def encode(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)
}

object HttpBrowser {
	def location(resp: HttpResponse[String]): String = resp.headers().firstValue("Location").toScala.getOrElse("")
}
