package cz.kamenitxan.jakon.webui.controler.impl

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.{JakonUser, Ordered}
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controler.impl.ObjectControler.fetchVisibleOrder
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import org.hibernate.criterion.Restrictions
import org.mindrot.jbcrypt.BCrypt
import spark.{ModelAndView, Request, Response}

import scala.language.postfixOps
import scala.tools.nsc.interpreter.session

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
			ses.beginTransaction()
			val criteria = getSession.createCriteria(classOf[JakonUser])
			val user = criteria.add(Restrictions.eq("email", email) ).uniqueResult().asInstanceOf[JakonUser]
			ses.getTransaction.commit()
			if (user == null) {
				PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
			} else if (checkPassword(password, user.password) && user.enabled){
				req.session(true).attribute("user", user)
				res.redirect("/admin/index")
			}
		}
		new Context(null, "login")
	}

	def logoutPost(req: Request, res: Response): ModelAndView = {
		req.session().invalidate()
		res.redirect("/admin")
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

		val session = DBHelper.getSession
		session.beginTransaction()
		try {
			val criteria = getSession.createCriteria(classOf[JakonUser])
			val tmpl = criteria.add(Restrictions.eq("name", "REGISTRATION") ).uniqueResult().asInstanceOf[EmailTemplateEntity]
			val email = new EmailEntity(tmpl.template, user.email, tmpl.subject, Map[String, AnyRef](
				"username" -> user.username,

			))

		} finally {
			session.getTransaction.commit()
			session.close()
		}
		val emailTaskEntity = new EmailEntity(
			"REGISTRATION",

		)

		new Context(null, "login")
	}

	def createUser(user: JakonUser): JakonUser = {
		user.password = hashPassword(user.password)
		val session = DBHelper.getSession
		session.beginTransaction()
		val id = session.save(user)
		session.getTransaction.commit()
		session.close()
		user.setId(id.asInstanceOf[Int])
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
