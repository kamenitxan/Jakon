package cz.kamenitxan.jakon.webui.controler.pagelets.data

import cz.kamenitxan.jakon.validation.validators.{EqualsWithOther, NotEmpty, Size}

class SetPasswordData {
	@NotEmpty()
	@Size(min = 5)
	var password: String = _
	@NotEmpty()
	@EqualsWithOther(value = "password")
	var password_check: String = _
	@NotEmpty
	var token: String = _
}
