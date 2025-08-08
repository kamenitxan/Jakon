package cz.kamenitxan.jakon.utils.security

import org.mindrot.jbcrypt.BCrypt

/**
 * Created by Kamenitxan on 05.08.2025
 */
object AuthUtils {

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
}
