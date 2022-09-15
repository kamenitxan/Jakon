package core.pagelet.entity

import cz.kamenitxan.jakon.validation.validators.NotEmpty

case class TestJsonPageletData(
																@NotEmpty
																msg: String
															)
