import * as ScalaJSApp from "./scalajs.js";
import JTextarea from "./forms/j_textarea";
import '../css/jakon.css';
import jQuery from 'jquery';

declare global {
	interface Window {
		jakon: {
            ScalaJSApp: typeof ScalaJSApp,
            JTextarea: typeof JTextarea;
		};
		$: typeof jQuery;
		jQuery: typeof jQuery;
	}
}

// Assign globals
window.jakon = {
    ScalaJSApp,
    JTextarea,
};

// @ts-ignore
window.Forms = ScalaJSApp.Forms;

window.$ = jQuery;
window.jQuery = jQuery;

console.log('Jakon loaded');