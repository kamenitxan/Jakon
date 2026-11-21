package cz.kamenitxan.jakon.webui.controller.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{EqualsWithOther, NotEmpty, Size}

case class SetPasswordData(
	@NotEmpty()
	@Size(min = 5)
	password: String,
	@NotEmpty()
	@EqualsWithOther(value = "password")
	password_check: String,
	@NotEmpty
	token: String
)
