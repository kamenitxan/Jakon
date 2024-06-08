let mix = require('laravel-mix');

mix.js([
	'src/frontend/admin/js/jakon.js',
	'src/frontend/admin/js/forms/j_textarea.js'
], 'js/jakon.js')
	.css('src/frontend/admin/css/jakon.css', 'css/jakon.css')
	.autoload({
		jquery: ['$', 'window.jQuery']
	})
	.extract()
	.setPublicPath('modules/backend/src/main/resources/static/jakon');