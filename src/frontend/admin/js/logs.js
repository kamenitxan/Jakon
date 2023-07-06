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
            const selected = document.querySelectorAll(".logTable tbody tr");
            selected.forEach(l => l.classList.remove("hidden"))
        } else {
            const other = document.querySelectorAll(".logTable tbody tr:not(" + severity + ")");
            const selected = document.querySelectorAll(".logTable tbody tr." + severity);
            other.forEach(l => l.classList.add("hidden"));
            selected.forEach(l => l.classList.remove("hidden"))

        }
    }
}

module.exports = Logs;