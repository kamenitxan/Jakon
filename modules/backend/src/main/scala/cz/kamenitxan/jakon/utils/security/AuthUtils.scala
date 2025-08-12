package cz.kamenitxan.jakon.utils.security

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

}

case class LoginAttemptInfo(unsuccessfulCount: Int, lastAttempt: LocalDateTime)
