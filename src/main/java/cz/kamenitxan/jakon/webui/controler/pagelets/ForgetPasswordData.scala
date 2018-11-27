package cz.kamenitxan.jakon.webui.controler.pagelets

import javax.validation.constraints.{Email, NotNull}

/**
  * Created by TPa on 2018-11-27.
  */
class ForgetPasswordData {
	@NotNull
	var email: String = _
}
