class ForeignObjectSelector {

    constructor(objectName, objectHash, includeEmptyValue) {
        this.objectName = objectName;
        this.objectHash = objectHash;
        this.includeEmptyValue = includeEmptyValue;
        this.endPoint = "/admin/api/search";
        this.selectbox = document.getElementById(this.objectHash);
        this.searchbox = document.getElementById("js_foreign_" + this.objectHash);

        /* this.selectbox.addEventListener("click", (e) => {
             this.showSearch(e);
         }); */
        this.selectbox.addEventListener("blur", (e) => {
            this.hideSearch(e);
        });
        this.searchbox.addEventListener("keyup", (e) => {
            this.handleSearch(e);
        });
        this.searchbox.addEventListener("blur", (e) => {
            this.handleSearch(e);
        });
        this.handleSearch();
    }

    showSearch() {
        this.searchbox.classList.remove("hidden");
        this.searchbox.focus();
    }

    hideSearch() {
        if (document.activeElement !== this.searchbox) {
            //this.searchbox.classList.add("hidden");
        }
    }

    handleSearch() {
        const Ajax = require("utils/Ajax.js");
        Ajax.post(this.endPoint,
            {
                objectName: this.objectName,
                query: this.searchbox.value
            })
            .then(data => {
                console.log(data);
                this.fillSelect(data);
            })
            .catch(error => console.error(error))
    }

    fillSelect(data) {
        const itemCount = this.selectbox.options.length;
        const selectedIds = this.selectbox.dataset["selected_id"].split(",").map(id => parseInt(id, 10))
        for (let i = itemCount - 1; i >= 0; i--) {
            this.selectbox.remove(i);
        }

        if (this.includeEmptyValue) {
            let opt = document.createElement("option");
            opt.text = "---";
            opt.value = "";
            this.selectbox.add(opt);
        }
        data.result.forEach(e => {
            let opt = document.createElement("option");
            opt.value = e.id;
            opt.text = `Id: ${e.id} - ${e.name}`;
            if (selectedIds.includes(e.id)) {
                opt.selected = true
            }
            this.selectbox.add(opt);
        })
    }

}

module.exports = ForeignObjectSelector;