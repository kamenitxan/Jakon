package cz.kamenitxan.jakon.webui.utils.pagelets

import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLSelectElement, document}

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
		if (all) {
			val selected = document.querySelectorAll(".logTable tbody tr")
			selected.foreach(l => l.classList.remove("hidden"))
		} else {
			val other = document.querySelectorAll(s".logTable tbody tr:not($severity)")
			val selected = document.querySelectorAll(s".logTable tbody tr.$severity")
			other.foreach(l => l.classList.add("hidden"))
			selected.foreach(l => l.classList.remove("hidden"))
		}
	}
}
