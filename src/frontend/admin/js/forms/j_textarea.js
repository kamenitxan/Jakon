var megamark = require('megamark');
var domador = require('domador');
var woofmark = require('woofmark');
woofmark(document.querySelector("#editor-container{{ fieldHash }}"),
    {
        parseMarkdown: megamark,
        defaultMode: 'wysiwyg',
        parseHTML: domador
    });