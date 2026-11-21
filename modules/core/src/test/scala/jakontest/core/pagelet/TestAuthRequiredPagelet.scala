package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}

import scala.collection.mutable

@Pagelet(path = "/authRequiredPagelet", authRequired = true)
class TestAuthRequiredPagelet extends AbstractPagelet {

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "authRequiredPageletValue"
		)
	}
}
