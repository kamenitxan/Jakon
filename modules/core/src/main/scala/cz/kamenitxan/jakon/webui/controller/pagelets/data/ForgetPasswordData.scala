package cz.kamenitxan.jakon.webui.controller.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{Email, NotEmpty}


/**
  * Created by TPa on 2018-11-27.
  */
case class ForgetPasswordData(
	@NotEmpty
	@Email
	email: String
)
