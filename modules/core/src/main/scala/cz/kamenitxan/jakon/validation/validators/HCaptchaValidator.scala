package cz.kamenitxan.jakon.validation.validators

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.net.URI
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}

class HCaptchaValidator extends Validator {
	override def fullKeys: Boolean = true

	private val error = "CAPTCHA_FAILED"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return ValidationResult(error)

		if (Settings.getHCaptchaSecret == null) {
			Logger.critical("hCaptcha secret is not defined")
			return Option.empty
		}

		try {
			val data = s"response=$value&secret=${Settings.getHCaptchaSecret}&sitekey=${Settings.getHCaptchaSiteKey}"
			val req = HttpRequest.newBuilder()
				.uri(new URI("https://hcaptcha.com/siteverify"))
				.POST(HttpRequest.BodyPublishers.ofString(data))
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.build()
			val client = HttpClient.newHttpClient()
			val rawRes = client.send(req, BodyHandlers.ofString())
			val res = new Gson().fromJson(rawRes.body(), classOf[HCaptchaResponse])

			if (res.success) {
				Option.empty
			} else {
				Logger.warn(rawRes.body())
				ValidationResult(error)
			}
		} catch {
			case ex: Exception =>
				Logger.warn("Failed to validate captcha", ex)
				ValidationResult(error)
		}


	}

	class HCaptchaResponse {
		var success: Boolean = _
	}
}
