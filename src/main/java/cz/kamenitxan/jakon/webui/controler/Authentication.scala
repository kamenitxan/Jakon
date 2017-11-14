package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.Context
import org.hibernate.criterion.Restrictions
import org.mindrot.jbcrypt.BCrypt
import spark.{ModelAndView, Request, Response}


/**
  * Created by TPa on 03.09.16.
  */
object Authentication {

	def loginGet(response: Response): ModelAndView = {
		new Context(null, "login")
	}

	def loginPost(req: Request, res: Response): ModelAndView = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		if (email != null && password != null) {
			val ses = DBHelper.getSession
			val criteria = getSession.createCriteria(classOf[JakonUser])
			val user = criteria.add(Restrictions.eq("email", email) ).uniqueResult().asInstanceOf[JakonUser]
			if (user == null) {

			} else if (checkPassword(password, user.password) && user.enabled){
				req.session(true).attribute("user", user.email)
				res.redirect("/admin/index")
			}
		}
		new Context(null, "login")
	}

	def registrationGet(response: Response): ModelAndView = {
		new Context(null, "register")
	}

	def registrationPost(req: Request, res: Response): ModelAndView = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		val password2 = req.queryParams("password2")
		val firstname = req.queryParams("firstname")
		val lastname = req.queryParams("lastname")
		if (!password.equals(password2)) {
			// TODO: chyba
			new Context(null, "register")
		}
		val user = new JakonUser()
		user.email = email
		user.username = email
		user.password = password
		user.firstName = firstname
		user.lastName = lastname
		createUser(user)
		new Context(null, "login")
	}

	def createUser(user: JakonUser): JakonUser = {
		user.password = hashPassword(user.password)
		val session = DBHelper.getSession
		session.beginTransaction()
		session.save(user)
		session.getTransaction.commit()
		session.close()
		user
	}

	def hashPassword(password_plaintext: String) = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(password_plaintext, salt)
	}

	def checkPassword(password_plaintext: String, stored_hash: String) =  {
		if(null == stored_hash || !stored_hash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(password_plaintext, stored_hash)
	}
}
