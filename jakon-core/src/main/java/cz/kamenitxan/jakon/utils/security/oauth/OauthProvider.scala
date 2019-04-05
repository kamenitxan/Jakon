package cz.kamenitxan.jakon.utils.security.oauth

import spark.Request

trait OauthProvider {

	val isEnabled: Boolean

	def authInfo(req: Request): OauthInfo
}
