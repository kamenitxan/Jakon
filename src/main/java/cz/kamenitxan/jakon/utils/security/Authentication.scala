package cz.kamenitxan.jakon.utils.security

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import org.hibernate.criterion.Restrictions
import org.mindrot.jbcrypt.BCrypt

object Authentication {

	def checkLogin(email: String, password: String): JakonUser =  {
		val ses = DBHelper.getSession
		ses.beginTransaction()
		val criteria = ses.createCriteria(classOf[JakonUser])
		val user: JakonUser = criteria.add(Restrictions.eq("email", email) ).uniqueResult().asInstanceOf[JakonUser]
		ses.getTransaction.commit()
		if (user == null) {
			return null
		} else if (checkPassword(password, user.password) && user.enabled){
			return user
		}
		null
	}

	private def checkPassword(password_plaintext: String, stored_hash: String): Boolean = {
		if(!stored_hash.startsWith("$2a$")) {
			throw new IllegalArgumentException("Invalid hash provided for comparison")
		}
		BCrypt.checkpw(password_plaintext, stored_hash)
	}
}