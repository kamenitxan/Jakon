class Jakon {

    static removeDomObject(evt) {
        let messages = evt.closest("#jakon_messages");
        messages.remove();
    }
}

module.exports = Jakon;