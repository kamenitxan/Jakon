package cz.kamenitxan.jakon.webui.forms

import cz.kamenitxan.jakon.webui.Ajax
import org.scalajs.dom
import org.scalajs.dom.{HTMLSelectElement, document, html}

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object ForeignObjectJs {

	val endPoint = "/admin/api/search";


	def handleSearch(objectName: String, query: String, selectBox: HTMLSelectElement, includeEmptyValue: Boolean): Unit =  {
		val reqData = js.Dynamic.literal(objectName = objectName, query = query)

		Ajax.post(this.endPoint, reqData).onComplete {
				case Failure(exception) =>
					println(exception)
				case Success(data) =>
					println(data)
					fillSelect(JSON.parse(data), selectBox, includeEmptyValue)
			}
	}

	private def fillSelect(data: js.Dynamic, selectBox: HTMLSelectElement, includeEmptyValue: Boolean): Unit =  {
		val selectedIds = selectBox.dataset("selected_id").split(",").filter(_.nonEmpty).map(id => id.toInt)
		selectBox.children.foreach(_.remove())

		if (includeEmptyValue) {
			val opt = document.createElement("option").asInstanceOf[html.Option]
			opt.text = "---"
			opt.value = ""
			selectBox.add(opt)
		}

		data.result.forEach((e: js.Dynamic) => {
			val id = e.selectDynamic("id").toString
			val opt = document.createElement("option").asInstanceOf[html.Option]
			opt.value = id
			opt.text = s"Id: $id - ${e.name}"
			if (selectedIds.contains(id.toInt)) {
				opt.selected = true
			}
			selectBox.add(opt)
		})
	}

}
