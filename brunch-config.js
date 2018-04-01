// See http://brunch.io for documentation.
exports.paths = {
    watched: ['src'],
    public: 'static/jakon'
};

exports.files = {
    javascripts: {
        joinTo: {
            'js/vendor.js': /^node_modules/, // Files that are not in `app` dir.
            'js/jakon.js': /^src\/frontend\/admin\/js/
        }
    },
    stylesheets: {
        joinTo: {
            'css/vendor.css': /^node_modules/,
            'css/jakon.css': /^src\/frontend\/admin\/css/
        }
    },


};

exports.plugins = {
    babel: {presets: ['latest']}
};

exports.npm = {
    enabled: true,
    globals: {
        jQuery: 'jquery',
        $: 'jquery',
        bootstrap: 'bootstrap'
    },
    styles: {
        bootstrap: ['dist/css/bootstrap.css']
    }
};
