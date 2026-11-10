package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Pagelet, Post}
import io.javalin.http.Context

/** Pagelet with path parameter. Path also contains param name as plain text ("lang" in "langPagelet"). */
@Pagelet(path = "/{lang}/langPagelet")
class TestPathParamPagelet extends AbstractPagelet {

	@Post(path = "/save")
	def save(ctx: Context, data: TestValidatedData): String = {
		s"saved-${ctx.pathParam("lang")}-${data.name}"
	}
}
