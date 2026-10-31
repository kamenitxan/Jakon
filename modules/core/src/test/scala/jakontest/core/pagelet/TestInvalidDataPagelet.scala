package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}

import scala.collection.mutable

@Pagelet(path = "/invalidDataPagelet")
class TestInvalidDataPagelet extends AbstractPagelet {

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(data: TestInvalidPageletData): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "invalidDataValue"
		)
	}

	@Get(path = "/valid", template = "pagelet/examplePagelet")
	def valid(): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "validMethodValue"
		)
	}
}

/** data class that is not case class - not supported */
class TestInvalidPageletData {
	var msg: String = scala.compiletime.uninitialized
}
