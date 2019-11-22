class Logs {

    constructor(props) {
        this.table = {};
        this.select = {};
    }


    init() {
        this.table = document.querySelector(".logTable");
        this.select = this.table.querySelector("#logSeverity");
        this.select.addEventListener("change", e => this.filterSeverity(e.target.value, false));
    }

    filterSeverity(severity) {
        const all = severity === "ALL";
        if (all) {
            console.log("ALLLLL")
        } else {
            console.log(severity)
        }
    }
}

module.exports = Logs;
module.exports = Logs;