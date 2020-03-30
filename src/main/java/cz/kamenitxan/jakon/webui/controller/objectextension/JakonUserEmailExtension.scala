package cz.kamenitxan.jakon.webui.controller.objectextension

import java.sql.Connection

import cz.kamenitxan.jakon.core.Director.SELECT_EMAIL_TMPL_SQL
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.{PageContext, SqlGen}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable

@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.BOTH)
@Pagelet
class JakonUserEmailExtension extends AbstractObjectExtension {

	// language=SQL
	private val Sql = "SELECT name from EmailTemplateEntity"

	override def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		val templates = DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.createStatement()
			DBHelper.select(stmt, Sql, classOf[EmailTemplateEntity]).map(_.entity)
		})
		context += "emailTemplates" -> templates.asJava
		context += "filterParams" -> req.queryMap().toMap.asScala
		  .filter(kv => kv._1.startsWith("filter_") && !kv._2.head.isEmpty)
		  .mapValues(v => new String(v.flatten))
		  .asJava
		super.render(context, templatePath, req)
	}

	@Get(path = "/admin/object/JakonUser/:id/sendEmail", template = "")
	def single(req: Request, res: Response): Unit = {
		val objectId = req.params(":id").toInt
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE id = ?")
			stmt.setInt(1, objectId)
			val users = DBHelper.selectDeep(stmt, classOf[JakonUser])
			sendEmails(req, res, users)
		})
	}

	@Get(path = "/admin/object/JakonUser/sendEmail", template = "")
	def list(req: Request, res: Response): Unit = {
		val filterParams = req.queryMap().toMap.asScala.filter(kv => kv._1.startsWith("filter_") && !kv._2.head.isEmpty).map(kv => kv._1.substring(7) -> kv._2.head)
		DBHelper.withDbConnection(implicit conn => {
			val filterSql = SqlGen.parseFilterParams(filterParams, classOf[JakonUser])
			val sql = s"SELECT * FROM JakonUser $filterSql"
			val stmt = conn.createStatement()
			val users = DBHelper.select(stmt, sql, classOf[JakonUser]).map(_.entity)
			sendEmails(req, res, users)
		})
	}

	private def sendEmails(req: Request, res: Response, users: Seq[JakonUser])(implicit conn: Connection): Unit = {
		val emailType = req.queryParams("emailType")
		if (emailType == "---") {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "JUEE_CHOOSE_TYPE")
		} else {
			val stmt = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
			stmt.setString(1, emailType)
			val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity

			users.foreach(user => {
				val email = new EmailEntity(emailType, user.email, tmpl.subject, Map[String, String](
					"firstName" -> user.firstName,
					"lastName" -> user.lastName,
					"email" -> user.email,
					"username" -> user.username,
					"protocol" -> (if (req.raw().isSecure) "https" else "http"),
					"host" -> req.host(),
					EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry
				))
				email.create()
			})
			PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "JUEE_EMAIL_SENT")
		}

		val redirectTo = if (req.headers("Referer") != null) {
			req.headers("Referer")
		} else {
			"/admin/object/JakonUser"
		}
		redirect(req, res, redirectTo)
	}

}
