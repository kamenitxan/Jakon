class JTextarea {
	init(fieldHash) {
		const megamark = require('megamark');
		const domador = require('domador');
		const woofmark = require('woofmark');
		const wmi = woofmark(document.querySelector("#editor-container" + fieldHash),
			{
				parseMarkdown: megamark,
				defaultMode: 'wysiwyg',
				parseHTML: domador,
				classes: {
					wysiwyg: ["form-control", "wk-textfield"]
				}
			});
		// removes default woofmark image button
		Array.from(document.querySelectorAll(".wk-command")).filter(e => e.innerHTML === "Image")[0].remove()
		// adds custom one
		document.wmi = wmi;
		wmi.addCommandButton("Image", (e, mode, chunks) => {
			new ImageSelector(chunks, wmi).init();
		})

	}
}
module.exports = JTextarea;

class ImageSelector {

	constructor(chunks, wmi) {
		this.endPoint = "/admin/api/images";
		this.holder = document.querySelector("#image_selector");
		this.chunks = chunks;
		this.wmi = wmi;
		this.Ajax = require("utils/Ajax.js");
	}

	init() {
		this.Ajax.post(this.endPoint, {})
			.then(data => this.fillTable(data))
			.catch(error => console.error(error));
	}

	fillTable(data) {
		console.log(data);
		this.holder.innerHTML = `
			<div class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel">
			  <div class="modal-dialog modal-lg" role="document">
				<div class="modal-content">
				  <div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					<h4 class="modal-title" id="gridSystemModalLabel">Aktuální složka: upload/</h4>
				  </div>
				  <div class="modal-body">
				  </div>
				</div>
			  </div>
			</div>
        `;
		const target = this.holder.querySelector(".modal-body");

		this.createList(data, target);
		$('.bs-example-modal-lg').modal('show');
	}

	changeFolder(path) {
		this.Ajax.post(this.endPoint, {
			path : path
		})
		.then(data => {
			console.log(data);
			const target = this.holder.querySelector(".modal-body");
			target.innerHTML = "";
			this.createList(data, target);

			const parentPath = path.substring(0, path.lastIndexOf("/"));
			const hasParent = parentPath !== "";
			const parentArrow = hasParent ? `<i class="fas fa-arrow-up pointer"></i>` : "";

			this.holder.querySelector(".modal-title").innerHTML = `
				<h4 class="modal-title" id="gridSystemModalLabel">${parentArrow} Aktuální složka: ${path}</h4>
			`;
			if (hasParent) {
				this.holder.querySelector(".modal-title i").addEventListener("click", (e) => {
					this.changeFolder(path.substring(0, path.lastIndexOf("/")));
				});
			}
		})
		.catch(error => console.error(error));
	}

	createList(data, target) {
		data.result.forEach(f => {
			let icon;
			if (f.fileType === "IMAGE") {
				icon = "fa-image";
			} else if (f.fileType === "FOLDER") {
				icon = "fa-folder";
			}

			const btn = f.fileType === "IMAGE" ? `<button class="btn btn-primary btn-sm">Vložit</button>` : "";
			const html = `
				<div class="row" id="fid${f.id}">
					<div class="col-md-10">
						<i class="far ${icon}"></i><span>${f.name}</span>
					</div>
					<div class="col-md-2">
						${btn}
					</div>
				</div>`

			target.insertAdjacentHTML("beforeend", html);

			if (f.fileType === "IMAGE") {
				const select = target.querySelector("#fid" + f.id + " button");
				select.addEventListener("click", e => {
					e.preventDefault();
					this.fillImage(f.name, "/" + f.path + f.name);
				});
			} else if (f.fileType === "FOLDER") {
				const row = target.querySelector("#fid" + f.id);
				row.classList.add("pointer");
				row.addEventListener("click", e => {
					e.preventDefault();
					this.changeFolder(f.path + f.name);
				});
			}

		});
	}


	fillImage(name, link) {
		this.wmi.runCommand((chunks, mode) => {
			// TODO: rozdilne mody
			chunks.before = chunks.before + "![" + name + "](" + link + ")";
			this.close();
		});
	}

	close() {
		$('.bs-example-modal-lg').modal('hide');
		this.holder.innerHTML = "";
	}
}