import 'vite/modulepreload-polyfill'
import '../css/jakon.css';
import jQuery from 'jquery';


window.jakon = {};

window.$ = jQuery;
window.jQuery = jQuery;