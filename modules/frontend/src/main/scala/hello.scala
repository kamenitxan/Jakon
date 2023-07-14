/**
 * Created by TPa on 11.07.2023.
 */
import scala.scalajs.js.annotation.*
import org.scalajs.dom
import org.scalajs.dom.{Event, PointerEvent}

@JSExportTopLevel("JakonUtils")
object HelloWorld {
	@JSExport
	def sayHello(e: PointerEvent): Unit = {
		println("Hello world!")
		e.target
		dom.document.querySelector()
	}
}