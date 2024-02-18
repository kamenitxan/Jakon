package cz.kamenitxan.jakon.webui.forms

import cz.kamenitxan.jakon.webui.Ajax
import cz.kamenitxan.jakon.webui.facade.jquery.$
import org.scalajs.dom.Element

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Created by Kamenitxan on 08.02.2024
 */
object FileSelectJs {

	val endPoint = "/admin/api/files"

	def init(holder: Element): Unit = {
		holder.querySelector(".selectBtn").addEventListener("click", e => {
			openFileSelect(holder)
		})
	}

	private def openFileSelect(holder: Element): Unit = {
		Ajax.post(endPoint, js.Dynamic.literal()).onComplete {
			case Failure(exception) => println(exception)
			case Success(data) => fillTable(JSON.parse(data), holder)
		}
	}

	private def fillTable(data: js.Dynamic, holder: Element): Unit = {
		println(data.toString)
		holder.querySelector(".fs_modal").innerHTML =
			"""
		<div class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog">
			<div class="modal-dialog modal-lg" role="document">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" >
							<span aria-hidden="true">
								&times;
							</span>
						</button>
						<h4 class="modal-title" id="gridSystemModalLabel">Aktuální složka: upload/</h4>
					</div>
					<div class="modal-body">
					</div>
				</div>
			</div>
		</div>
		""".stripMargin

		holder.querySelector("button.close").addEventListener("click", e => {
			close(holder)
		})

		val target = holder.querySelector(".modal-body")
		createList(data, target, holder)
		$(".bs-example-modal-lg").modal("show")
	}

	private def createList(data: js.Dynamic, target: Element, holder: Element): Unit = {
		data.result.forEach((f: js.Dynamic) => {
			val fileType = f.selectDynamic("fileType").toString
			val icon = if (fileType == "IMAGE") {
				"fa-image"
			} else if (fileType == "FOLDER") {
				"fa-folder"
			}
			val btn = if (fileType == "IMAGE") "<button class=\"btn btn-primary btn-sm\">Vložit</button>" else ""
			val html =
				s"""
			<div class="row" id="fid${f.id}">
				<div class="col-md-10">
					<i class="far $icon"></i> <span>${f.name}
				</span>
				</div>
				<div class="col-md-2">
					$btn
				</div>
			</div>
			""".stripMargin

			target.insertAdjacentHTML("beforeend", html)

			if (fileType == "IMAGE") {
				val select = target.querySelector("#fid" + f.id + " button")
				select.addEventListener("click", e => {
					e.preventDefault();
					selectFile(f.selectDynamic("name").toString, "/" + f.path + f.name, holder)
				})
			} else if (fileType == "FOLDER") {
				val row = target.querySelector("#fid" + f.id)
				row.classList.add("pointer")
				row.addEventListener("click", e => {
					e.preventDefault()
					// TODO changeFolder(f.path + f.name)
				})
			}
		})
	}

	private def selectFile(name: String, link: String, holder: Element): Unit = {
		//this.chunks.before = this.chunks.before + "![" + name + "](" + link + ")";
		this.close(holder);
	}

	private def close(holder: Element): Unit = {
		$(".bs-example-modal-lg").modal("hide")
		holder.querySelector(".fs_modal").innerHTML = "";
	}

}
