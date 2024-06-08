let mix = require('laravel-mix');

mix.js([
	'modules/frontend/src/main/js/jakon.js',
	'modules/frontend/src/main/js/forms/j_textarea.js'
], 'js/jakon.js')
	.css('modules/frontend/src/main/css/jakon.css', 'css/jakon.css')
	.autoload({
		jquery: ['$', 'window.jQuery']
	})
	.extract()
	.setPublicPath('modules/backend/src/main/resources/static/jakon');