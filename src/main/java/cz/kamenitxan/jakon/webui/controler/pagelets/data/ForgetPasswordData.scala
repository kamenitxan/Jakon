package cz.kamenitxan.jakon.webui.controler.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{Email, NotEmpty}


/**
  * Created by TPa on 2018-11-27.
  */
class ForgetPasswordData {
	@NotEmpty
	@Email
	var email: String = _
}
