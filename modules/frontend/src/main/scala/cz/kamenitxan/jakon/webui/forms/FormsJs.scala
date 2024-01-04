package cz.kamenitxan.jakon.webui.forms

import cz.kamenitxan.jakon.webui.forms.ForeignObjectJs
import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLInputElement, HTMLSelectElement, document}

import scala.scalajs.js.annotation.*

@JSExportTopLevel("Forms")
object FormsJs {

	@JSExport
	def initForeignObject(objectName: String, objectHash: String, includeEmptyValue: Boolean): Unit = {
		val selectBox = document.getElementById(objectHash).asInstanceOf[HTMLSelectElement]
		val searchBox = document.getElementById("js_foreign_" + objectHash).asInstanceOf[HTMLInputElement];


		ForeignObjectJs.handleSearch(objectName, searchBox.value, selectBox, includeEmptyValue)
	}

}
