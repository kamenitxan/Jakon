package cz.kamenitxan.jakon

import zio.{http, *}
import zio.http.*

import scala.Predef.ArrowAssoc

object ZioServer extends ZIOAppDefault {

	val app: App[Any] = {

		val p: (Method.GET.type, Path) = Method.GET ->  Path.decode("/test/text")
		val v: Path = Root / "text"
		Http.collect[Request] {
			case Method.GET -> p => Response.text("Hello test!")
			case Method.GET -> Root / "text" => Response.text("Hello World!")
		}
	}

	override val run =
		Server.serve(app).provide(Server.default)
}
