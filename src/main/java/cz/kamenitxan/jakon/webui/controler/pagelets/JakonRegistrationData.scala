package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.core.dynamic.validation.Email
import javax.validation.constraints.NotEmpty

class JakonRegistrationData {
	@NotEmpty(message = "NOT_EMPTY")
	@Email(message = "INVALID")
	var email: String = _
	@NotEmpty(message = "NOT_EMPTY")
	var password: String = _
	@NotEmpty(message = "NOT_EMPTY")
	var password2: String = _
	@NotEmpty(message = "NOT_EMPTY")
	var firstname: String = _
	@NotEmpty(message = "NOT_EMPTY")
	var lastname: String = _
}
