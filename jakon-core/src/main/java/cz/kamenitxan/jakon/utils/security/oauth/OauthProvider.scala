package cz.kamenitxan.jakon.utils.security.oauth

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.webui.controler.impl.Authentication.{SQL_FIND_ACL, SQL_FIND_USER}
import org.slf4j.{Logger, LoggerFactory}
import spark.Request

trait OauthProvider {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val isEnabled: Boolean

	def authInfo(req: Request): OauthInfo

	def handleAuthResponse(req: Request): Boolean

	def logIn(req: Request, email: String): Boolean = {
		implicit val conn = DBHelper.getConnection
		try {
			val stmt = conn.prepareStatement(SQL_FIND_USER)
			stmt.setString(1, email)

			val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
			if (result.entity == null) {
				false
			} else {
				val user = result.entity.asInstanceOf[JakonUser]
				val stmt = conn.prepareStatement(SQL_FIND_ACL)
				stmt.setInt(1, result.foreignIds.getOrElse("acl_id", null).id)
				val aclResult = DBHelper.selectSingle(stmt, classOf[AclRule])
				user.acl = aclResult.entity.asInstanceOf[AclRule]

				logger.info("User " + user.username + " logged in")
				req.session(true).attribute("user", user)
				true
			}
		} finally {
			conn.close()
		}
	}
}
