package validation

import cz.kamenitxan.jakon.validation.validators.*

/**
 * Created by TPa on 14.08.2022.
 */
class ValidationTestData {
	@NotEmpty
	var string: String = _
	@Min(value = 5)
	@Max(value = 5)
	var number: String = _
	var password: String = "testok"
	@EqualsWithOther(value = "password")
	var password2: String = _
	@Size(min = 5, max = 10)
	var size: String = _
	@Email
	var email: String = _
}
