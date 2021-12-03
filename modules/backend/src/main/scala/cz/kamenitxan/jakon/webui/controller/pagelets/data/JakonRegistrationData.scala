package cz.kamenitxan.jakon.webui.controller.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{Email, EqualsWithOther, NotEmpty, Size}

class JakonRegistrationData {
	@NotEmpty()
	@Email()
	var email: String = _
	@NotEmpty()
	@Size(min = 5)
	var password: String = _
	@NotEmpty()
	@Size(min = 5)
	@EqualsWithOther(value = "password")
	var password2: String = _
	@NotEmpty()
	var firstname: String = _
	@NotEmpty()
	var lastname: String = _
}
