package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.EmailTemplateService
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.{PageContext, SqlGen}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import java.sql.Connection
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.BOTH)
@Pagelet
class JakonUserEmailExtension extends AbstractObjectExtension {

	// language=SQL
	private val Sql = "SELECT name from EmailTemplateEntity"

	override def render(context: mutable.Map[String, Any], templatePath: String, ctx: Context): String = {
		if (Settings.isEmailEnabled) {
			val templates = DBHelper.withDbConnection(implicit conn => {
				val stmt = conn.createStatement()
				DBHelper.select(stmt, Sql, classOf[EmailTemplateEntity]).map(_.entity)
			})
			context += "emailTemplates" -> templates.asJava
			context += "filterParams" -> ctx.queryParamMap().asScala
			  .filter(kv => kv._1.startsWith("filter_") && kv._2.asScala.head.nonEmpty)
			  .view.mapValues(v => v.asScala.mkString)
			  .asJava
			super.render(context, templatePath, ctx)
		} else {
			""
		}
	}

	@Get(path = "/admin/object/JakonUser/{id}/sendEmail", template = "")
	def single(ctx: Context): Unit = {
		val objectId = ctx.pathParam("id").toInt
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE id = ?")
			stmt.setInt(1, objectId)
			val users = DBHelper.selectDeep(stmt)(implicitly, classOf[JakonUser])
			sendEmails(ctx, users)
		})
	}

	@Get(path = "/admin/object/JakonUser/sendEmail", template = "")
	def list(ctx: Context): Unit = {
		val filterParams = ctx.queryParamMap().asScala.filter(kv => kv._1.startsWith("filter_") && kv._2.asScala.head.nonEmpty).map(kv => kv._1.substring(7) -> kv._2.asScala.head)
		DBHelper.withDbConnection(implicit conn => {
			val filterSql = SqlGen.parseFilterParams(filterParams, classOf[JakonUser])
			val sql = s"SELECT * FROM JakonUser $filterSql"
			val stmt = conn.createStatement()
			val users = DBHelper.select(stmt, sql, classOf[JakonUser]).map(_.entity)
			sendEmails(ctx, users)
		})
	}

	private def sendEmails(ctx: Context, users: Seq[JakonUser])(implicit conn: Connection): Unit = {
		val emailType = ctx.queryParam("emailType")
		if (emailType == "---") {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "JUEE_CHOOSE_TYPE")
		} else {
			val tmpl = EmailTemplateService.getByName(emailType)

			users.foreach(user => {
				val email = new EmailEntity(emailType, user.email, tmpl.subject, Map[String, String](
					"firstName" -> user.firstName,
					"lastName" -> user.lastName,
					"email" -> user.email,
					"username" -> user.username,
					"protocol" -> (if (ctx.req().isSecure) "https" else "http"),
					"host" -> ctx.host(),
					EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry
				))
				email.create()
			})
			PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "JUEE_EMAIL_SENT")
		}

		val redirectTo = if (ctx.header("Referer") != null) {
			ctx.header("Referer")
		} else {
			"/admin/object/JakonUser"
		}
		redirect(ctx, redirectTo)
	}

}
