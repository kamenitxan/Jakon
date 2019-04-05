class Validation {

    constructor(formId) {
        this.formId = formId;
        this.form = document.getElementById(this.formId)
    }

}

module.exports = Validation;