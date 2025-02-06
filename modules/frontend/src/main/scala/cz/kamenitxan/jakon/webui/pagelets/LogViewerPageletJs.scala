package cz.kamenitxan.jakon.webui.pagelets

import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLSelectElement, URLSearchParams, document, window}

import scala.scalajs.js.annotation.*

@JSExportTopLevel("LogViewerPagelet")
object LogViewerPageletJs {

	@JSExport
	def init(): Unit = {
		val select = document.querySelector(".logTable #logSeverity")
		select.addEventListener("change", evt => {
			evt.target match
				case select: HTMLSelectElement => filterSeverity(select.value)
		})
	}

	private def filterSeverity(severity: String): Unit = {
		val all = severity == "ALL"
		val params = new URLSearchParams(window.location.search)
		if (all) {
			params.delete("severity")
		} else {
			params.set("severity", severity)
		}
		window.location.search = params.toString
	}
}
