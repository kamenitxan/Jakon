class JTextarea {
    init(fieldHash) {
        const megamark = require('megamark');
        const domador = require('domador');
        const woofmark = require('woofmark');
        woofmark(document.querySelector("#editor-container" + fieldHash),
            {
                parseMarkdown: megamark,
                defaultMode: 'wysiwyg',
                parseHTML: domador,
                classes: {
                    wysiwyg: ["form-control", "wk-textfield"]
                }
            });
    }
};


module.exports = JTextarea;