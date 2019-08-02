package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.validation.validators.{Email, NotEmpty, Size}

class JakonRegistrationData {
	@NotEmpty()
	@Email()
	var email: String = _
	@NotEmpty()
	@Size(min = 5)
	var password: String = _
	@NotEmpty()
	@Size(min = 5)
	var password2: String = _
	@NotEmpty()
	var firstname: String = _
	@NotEmpty()
	var lastname: String = _
}
