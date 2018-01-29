package cz.kamenitxan.jakon.utils.security

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import org.hibernate.criterion.Restrictions
import org.mindrot.jbcrypt.BCrypt

object Authentication {

	fun checkLogin(email: String, password: String): JakonUser?  {
		val ses = DBHelper.getSession()
		val criteria = ses.createCriteria(JakonUser::class.java)
		val user: JakonUser? = criteria.add(Restrictions.eq("email", email) ).uniqueResult() as JakonUser
		if (user == null) {
			return null
		} else if (checkPassword(password, user.password) && user.enabled){
			return user
		}
		return null
	}

	private fun checkPassword(password_plaintext: String, stored_hash: String): Boolean {
		if(!stored_hash.startsWith("$2a$"))
			throw  java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		return BCrypt.checkpw(password_plaintext, stored_hash)
	}
}