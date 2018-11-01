package cz.kamenitxan.jakon.example.dynamic

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import spark.{ModelAndView, Request, Response}
import javax.validation.Validation
import javax.validation.ValidatorFactory
import org.slf4j.LoggerFactory

@Pagelet(path = "/pagelet")
class ExamplePagelet extends AbstractPagelet{
	private val logger = LoggerFactory.getLogger(this.getClass)

	@Get(path = "/get", template = "")
	def get(req: Request, res: Response) = {

	}

		@Post(path = "/post", template = "")
		def post(req: Request, res: Response, data: PageletData) = {
			val factory = Validation.buildDefaultValidatorFactory
			val validator = factory.getValidator
			val violations = validator.validate(data)
			violations.forEach(v => logger.error(v.getMessage))
		}

	override def handle(request: Request, response: Response): ModelAndView = ???
}
