package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.core.dynamic.validation.Email
import javax.validation.constraints.NotEmpty

class JakonRegistrationData {
	@NotEmpty
	@Email
	var email: String = _
	@NotEmpty
	var password: String = _
	@NotEmpty
	var password2: String = _
	@NotEmpty
	var firstname: String = _
	@NotEmpty
	var lastname: String = _
}
