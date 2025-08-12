import 'vite/modulepreload-polyfill'
import JTextarea from "./forms/j_textarea";
import '../css/jakon.css';
import jQuery from 'jquery';


window.jakon = {};
window.jakon.JTextarea = JTextarea;

window.$ = jQuery;
window.jQuery = jQuery;