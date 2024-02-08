package cz.kamenitxan.jakon.webui.forms

import cz.kamenitxan.jakon.webui.Ajax
import org.scalajs.dom.Element

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Created by Kamenitxan on 08.02.2024
 */
object FileSelectJs {

	val endPoint = "/admin/api/files";

	def openFileSelect(holder: Element): Unit = {
		Ajax.post(endPoint, {}).onComplete {
			case Failure(exception) => println(exception)
			case Success(data) => fillTable(JSON.parse(data), holder)
		}
	}

	private def fillTable(data: js.Dynamic, holder: Element): Unit = {
		println(data)
		holder.innerHTML = """
		<div class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel">
			<div class="modal-dialog modal-lg" role="document">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-label="Close">
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
		
		val target = holder.querySelector(".modal-body")
		createList(data, target)
		// TODO
		// $('.bs-example-modal-lg').modal('show');
	}
	
	private def createList(data: js.Dynamic, target: Element): Unit = {
		data.result.forEach((f: js.Dynamic) => {
			val fileType = f.selectDynamic("fileType").toString
			val icon = if (fileType == "IMAGE") {
				"fa-image";
			} else if (fileType == "FOLDER") {
				"fa-folder"
			}
			val btn = if (fileType == "IMAGE") "<button class=\"btn btn-primary btn-sm\">Vložit</button>" else ""
			val html = """
			<div class="row" id="fid${f.id}">
				<div class="col-md-10">
					<i class="far ${icon}"></i> <span>$
					{f.name}
				</span>
				</div>
				<div class="col-md-2">
					$
					{btn}
				</div>
			</div>
			""".stripMargin

			target.insertAdjacentHTML("beforeend", html)

			if (fileType == "IMAGE") {
				val select = target.querySelector("#fid" + f.id + " button")
				select.addEventListener("click", e => {
					e.preventDefault();
					selectFile(f.selectDynamic("name").toString, "/" + f.path + f.name)
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

	def selectFile(name: String, link: String) = {
		//this.chunks.before = this.chunks.before + "![" + name + "](" + link + ")";
		this.close();
	}

	def close() = {
		// TODO $('.bs - example - modal - lg').modal('hide')
		//this.holder.innerHTML = "";
	}
	
}
