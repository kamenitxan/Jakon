class ForeignObjectSelector {

    constructor(objectName, objectHash) {
        this.objectName = objectName;
        this.objectHash = objectHash;
        this.endPoint = "/admin/api/search";
        this.selectbox = document.getElementById(this.objectHash);
        this.searchbox = document.getElementById("js_foreign_" + this.objectHash);

        this.selectbox.addEventListener("click", (e) => {
            this.showSearch(e);
        });
        this.selectbox.addEventListener("blur", (e) => {
            this.hideSearch(e);
        });
        this.searchbox.addEventListener("keyup", (e) => {
            this.handleSearch(e);
        });
    }

    showSearch(evt) {
        this.searchbox.classList.remove("hidden");
        this.searchbox.focus();
    }

    hideSearch(evt) {
        if (document.activeElement !== this.searchbox) {
            //this.searchbox.classList.add("hidden");
        }
    }

    handleSearch(evt) {
        const Ajax = require("utils/Ajax.js");
        Ajax.post(this.endPoint,
            {
                objectName: this.objectName,
                query: this.searchbox.value
            })
            .then(data => console.log(data))
            .catch(error => console.error(error))
    }

}

module.exports = ForeignObjectSelector;