import JTextarea from "./forms/j_textarea";
import '../css/jakon.css';
import jQuery from 'jquery';

declare global {
	interface Window {
		jakon: {
			JTextarea: typeof JTextarea;
		};
		$: typeof jQuery;
		jQuery: typeof jQuery;
	}
}

// Assign globals
window.jakon = {
	JTextarea,
};

window.$ = jQuery;
window.jQuery = jQuery;

console.log('Jakon loaded');