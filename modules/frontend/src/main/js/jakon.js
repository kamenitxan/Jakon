import 'vite/modulepreload-polyfill'
// must be first, patches XHR/fetch before anything sends a request
import './csrf.js';
import '../css/jakon.css';
import jQuery from 'jquery';


window.jakon = {};

window.$ = jQuery;
window.jQuery = jQuery;