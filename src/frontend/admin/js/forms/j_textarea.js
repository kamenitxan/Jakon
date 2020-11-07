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
        wmi.addCommandButton("Image", (e, mode, chunks) => {
            new ImageSelector(chunks, wmi).init();
            console.log(chunks.before)
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
    }

    init() {
        const Ajax = require("utils/Ajax.js");
        Ajax.post(this.endPoint, {})
            .then(data => this.fillTable(data))
            .catch(error => console.error(error))

        this.holder.classList.remove("hidden");

    }

    fillTable(data) {
        console.log(data);
        this.holder.innerHTML = "";
        this.createNavBar();
        this.createList(data);
    }

    createNavBar() {
        // TODO: zobrazeni aktualni složky
        const navbar = document.createElement("div")
        const closeBtn = document.createElement("button")
        closeBtn.innerText = "X"
        closeBtn.addEventListener("click", e => this.close());
        navbar.appendChild(closeBtn);
        this.holder.appendChild(navbar);
    }

    createList(data) {
        data.result.forEach(f => {
            // TODO: složky
           const row = document.createElement("div");
           row.classList.add("row");

           const name = document.createElement("span");
           name.innerText = f.name;

           const select = document.createElement("button");
           select.innerText = "Vložit";
           select.addEventListener("click", e => this.fillImage(f.name, "/" + f.path + f.name))

           row.appendChild(name);
           row.appendChild(select);
           this.holder.appendChild(row);
        });
    }

    fillImage(name, link) {
        this.wmi.runCommand((chunks, mode) => {
            // TODO: rozdilne mody
            chunks.before = this.chunks.before + "![" + name + "](" + link + ")";
        });
        this.close();
    }

    close() {
        this.holder.innerHTML = "";
        this.holder.classList.add("hidden");
    }
}