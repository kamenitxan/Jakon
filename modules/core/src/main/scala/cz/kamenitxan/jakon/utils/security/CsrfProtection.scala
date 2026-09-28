package cz.kamenitxan.jakon.utils.security

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.logging.Logger
import io.javalin.http.{Context, HandlerType, HttpStatus}

import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import java.util.Base64

/**
 * Synchronizer token CSRF protection for admin state-changing requests.
 *
 * Token is stored in session and must be sent with every non-safe request
 * as form param `_csrf` or header `X-CSRF-Token`.
 */
object CsrfProtection {
	val SessionKey = "jakon_csrf_token"
	val FormParam = "_csrf"
	val HeaderName = "X-CSRF-Token"

	private val SafeMethods = Set(HandlerType.GET, HandlerType.HEAD, HandlerType.OPTIONS)
	private val random = new SecureRandom()

	def getOrCreateToken(ctx: Context): String = {
		val existing: String = ctx.sessionAttribute(SessionKey)
		if (existing != null) {
			existing
		} else {
			val bytes = new Array[Byte](32)
			random.nextBytes(bytes)
			val token = Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)
			ctx.sessionAttribute(SessionKey, token)
			token
		}
	}

	/** Drops current token, new one is generated on next use. Call on privilege change (login). */
	def rotateToken(ctx: Context): Unit = {
		ctx.sessionAttribute(SessionKey, null)
	}

	def isValid(ctx: Context): Boolean = {
		val expected: String = ctx.sessionAttribute(SessionKey)
		if (expected == null) return false
		val provided = Option(ctx.header(HeaderName)).orElse(Option(ctx.formParam(FormParam))).orNull
		provided != null && MessageDigest.isEqual(
			expected.getBytes(StandardCharsets.UTF_8),
			provided.getBytes(StandardCharsets.UTF_8)
		)
	}

	/**
	 * Before handler. Checks token only for logged in users, anonymous requests have nothing to forge.
	 * Disabled in DEVEL mode.
	 */
	def check(ctx: Context): Unit = {
		if (Settings.getDeployMode == DeployMode.DEVEL) return
		if (SafeMethods.contains(ctx.method())) return
		if (ctx.sessionAttribute[AnyRef]("user") == null) return
		if (!isValid(ctx)) {
			Logger.warn(s"CSRF token validation failed for ${ctx.method()} ${ctx.path()}")
			ctx.status(HttpStatus.FORBIDDEN).result("Invalid CSRF token")
			ctx.skipRemainingHandlers()
		}
	}
}
