package cz.kamenitxan.jakon.webui.controller.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{Email, EqualsWithOther, NotEmpty, Size}

case class JakonRegistrationData(
	@NotEmpty()
	@Email()
	email: String,
	@NotEmpty()
	@Size(min = 5)
	password: String,
	@NotEmpty()
	@Size(min = 5)
	@EqualsWithOther(value = "password")
	password2: String,
	@NotEmpty()
	firstname: String,
	@NotEmpty()
	lastname: String
)
