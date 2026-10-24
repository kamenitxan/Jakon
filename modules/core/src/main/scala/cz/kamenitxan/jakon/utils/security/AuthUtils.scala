package cz.kamenitxan.jakon.utils.security

import cz.kamenitxan.jakon.core.model.JakonUser
import io.javalin.http.{Context, Cookie, SameSite}
import org.mindrot.jbcrypt.BCrypt

import java.time.LocalDateTime
import scala.collection.mutable

/**
 * Created by Kamenitxan on 05.08.2025
 */
object AuthUtils {

	private val maxLoginAttempts = 5
	private val lockoutMinutes = 10
	private val loginAttempts = mutable.Map[Int, LoginAttemptInfo]()
	/** How long a "remember me" login stays valid, in seconds (30 days). */
	private val rememberMeMaxAgeSeconds = 30 * 24 * 3600
	private val sessionCookieName = "JSESSIONID"

	/**
	 * Hash a password using the OpenBSD bcrypt scheme
	 *
	 * @param passwordPlaintext the password to hash
	 * @return the hashed password
	 */
	def hashPassword(passwordPlaintext: String): String = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(passwordPlaintext, salt)
	}

	/**
	 * Check that a plaintext password matches a previously hashed one
	 *
	 * @param passwordPlaintext the plaintext password to verify
	 * @param storedHash        the previously-hashed password
	 * @return true if the passwords match, false otherwise
	 */
	def checkPassword(passwordPlaintext: String, storedHash: String): Boolean = {
		if (null == storedHash || !storedHash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(passwordPlaintext, storedHash)
	}

	def isUnderLoginAttemptLimit(userId: Int): Boolean = {
		loginAttempts.get(userId) match {
			case None => true
			case Some(LoginAttemptInfo(count, last)) =>
				if (count < maxLoginAttempts) {
					true
				} else {
					val unlockTime = last.plusMinutes(lockoutMinutes)
					if (LocalDateTime.now().isAfter(unlockTime)) {
						resetLoginAttempts(userId)
						true
					} else {
						false
					}
				}
		}
	}


	def incrementLoginAttempts(userId: Int): Unit = {
		val info = loginAttempts.getOrElseUpdate(userId, LoginAttemptInfo(0, LocalDateTime.now()))
		loginAttempts.put(userId, LoginAttemptInfo(info.unsuccessfulCount + 1, LocalDateTime.now()))
	}

	def resetLoginAttempts(userId: Int): Unit = {
		loginAttempts.remove(userId)
	}

	/**
	 * Stores user in session. Session id is changed (session attributes are kept) and CSRF token
	 * is rotated to prevent session fixation.
	 *
	 * @param remember if true, the session is extended and the session cookie is re-issued as a
	 *                 persistent cookie (survives browser restart) instead of the default session
	 *                 cookie (deleted when the browser closes).
	 */
	def logIn(ctx: Context, user: JakonUser, remember: Boolean = false): Unit = {
		val req = ctx.req()
		req.getSession(true)
		req.changeSessionId()
		CsrfProtection.rotateToken(ctx)
		ctx.sessionAttribute("user", user)

		if (remember) {
			req.getSession.setMaxInactiveInterval(rememberMeMaxAgeSeconds)
			// The container's session cookie has no Max-Age by default (expires when the browser
			// closes). Re-issuing it with a Max-Age turns it into a persistent "remember me" cookie;
			// the browser keeps only the last Set-Cookie it receives for the same name/path.
			ctx.cookie(new Cookie(sessionCookieName, req.getSession.getId, "/", rememberMeMaxAgeSeconds, ctx.req().isSecure, true, null, SameSite.LAX))
		}
	}

}

case class LoginAttemptInfo(unsuccessfulCount: Int, lastAttempt: LocalDateTime)
