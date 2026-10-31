package jakontest.validation

import cz.kamenitxan.jakon.validation.validators.*

/**
 * Created by TPa on 14.08.2022.
 */
class ValidationTestData {
	@NotEmpty
	var string: String = scala.compiletime.uninitialized
	@Min(value = 5)
	@Max(value = 5)
	var number: String = scala.compiletime.uninitialized
	var password: String = "testok"
	@EqualsWithOther(value = "password")
	var password2: String = scala.compiletime.uninitialized
	@Size(min = 5, max = 10)
	var size: String = scala.compiletime.uninitialized
	@Email
	var email: String = scala.compiletime.uninitialized
}
