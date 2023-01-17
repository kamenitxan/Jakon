// See http://brunch.io for documentation.
exports.paths = {
    watched: ['src/frontend'],
    public: 'modules/backend/src/main/resources/static/jakon'
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
    babel: {
        presets: [
            ["@babel/preset-env", {
                targets: {
                    browsers: ['last 2 versions']
                }
            }]
        ]
    }
};

exports.npm = {
    enabled: true,
    globals: {
        jQuery: 'jquery',
        $: 'jquery',
        // bootstrap: 'bootstrap'
    },
    styles: {
        bootstrap: ['dist/css/bootstrap.css']
    }
};

exports.modules = {
    nameCleaner(path) {
        return path
        // Strip app/ and app/externals/ prefixes
            .replace(/^app\/(?:externals\/)?/, '')
            // Allow -x.y[.z…] version suffixes in mantisses
            .replace("src/frontend/admin/js/", '')
    }
};

