package utils

import org.scalajs.dom
import org.scalajs.dom.{Event, PointerEvent, Element}

import scala.scalajs.js.annotation.*

/**
* Created by TPa on 11.07.2023.
*/

@JSExportTopLevel("JakonUtils")
object JakonUtils {
	@JSExport
	def removeJakonMessages(el: dom.Element): Unit = {
		val messages = el.closest("#jakon_messages")
		messages.remove()
	}
}