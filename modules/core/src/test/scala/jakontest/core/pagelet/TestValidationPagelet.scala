package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import io.javalin.http.Context

import scala.collection.mutable

@Pagelet(path = "/validationPagelet")
class TestValidationPagelet extends AbstractPagelet {

	@Post(path = "/save")
	def save(data: TestValidatedData): String = {
		s"saved-${data.name}-${data.count}"
	}

	@Get(path = "/primitives", template = "pagelet/examplePagelet")
	def primitives(data: TestPrimitiveData): mutable.Map[String, Any] = {
		mutable.Map("pushed" -> data.toString)
	}

	@Get(path = "/redirect", template = "pagelet/examplePagelet")
	def redirectGet(ctx: Context): mutable.Map[String, Any] = {
		redirect(ctx, "/pagelet/get")
		null
	}

	@Get(path = "/brokenTemplate", template = "pagelet/nonExistentTemplate")
	def brokenTemplate(): mutable.Map[String, Any] = {
		mutable.Map.empty
	}
}

case class TestValidatedData(@NotEmpty name: String, count: Int)

case class TestPrimitiveData(b: Boolean, c: Char, by: Byte, s: Short, i: Int, l: Long, f: Float, d: Double)
