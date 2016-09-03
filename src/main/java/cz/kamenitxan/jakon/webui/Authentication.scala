package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import org.mindrot.jbcrypt.BCrypt

/**
  * Created by TPa on 03.09.16.
  */
object Authentication {

	def login(username: String, password: String): Option[JakonUser] = {
		val user = DBHelper.getDao(classOf[JakonUser]).queryBuilder().where().eq("username", username).queryForFirst().asInstanceOf[JakonUser]
		if (checkPassword(password, user.getPassword)) return Some(user)
		None
	}

	def createUser(user: JakonUser): JakonUser = {
		user.password = hashPassword(user.password)
		DBHelper.getDao(classOf[JakonUser]).createIfNotExists(user).asInstanceOf[JakonUser]
	}

	def hashPassword(password_plaintext: String): String = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(password_plaintext, salt)
	}

	def checkPassword(password_plaintext: String, stored_hash: String): Boolean =  {
		if(null == stored_hash || !stored_hash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(password_plaintext, stored_hash)
	}
}
