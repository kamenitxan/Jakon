package jakontest.utils.entity

import cz.kamenitxan.jakon.core.database.JakonField

/**
 * Created by TPa on 02.06.2020.
 */
class TestEmbeddedObject {
	@JakonField
	var string: String = scala.compiletime.uninitialized
	@JakonField
	var int: Int = scala.compiletime.uninitialized
}
