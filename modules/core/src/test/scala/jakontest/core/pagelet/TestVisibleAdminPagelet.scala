package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet

import scala.collection.mutable

@Pagelet(path = "/admin/visiblePagelet", showInAdmin = true)
class TestVisibleAdminPagelet extends AbstractAdminPagelet {

	override val name: String = this.getClass.getSimpleName

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "visiblePageletValue"
		)
	}
}
