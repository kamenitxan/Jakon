/*
 * Adds CSRF token (from <meta name="csrf-token">) to all same-origin state-changing requests:
 * POST forms get hidden "_csrf" input, XMLHttpRequest and fetch get "X-CSRF-Token" header.
 */
(function () {
	'use strict';

	var FORM_PARAM = '_csrf';
	var HEADER = 'X-CSRF-Token';
	var SAFE_METHODS = /^(GET|HEAD|OPTIONS)$/i;

	function getToken() {
		var meta = document.querySelector('meta[name="csrf-token"]');
		return meta ? meta.getAttribute('content') : null;
	}

	function isSameOrigin(url) {
		try {
			return new URL(url, window.location.href).origin === window.location.origin;
		} catch (e) {
			return false;
		}
	}

	function addTokenToForm(form) {
		var token = getToken();
		if (!token || (form.method || '').toLowerCase() !== 'post' || !isSameOrigin(form.action || window.location.href)) {
			return;
		}
		var input = form.querySelector('input[name="' + FORM_PARAM + '"]');
		if (!input) {
			input = document.createElement('input');
			input.type = 'hidden';
			input.name = FORM_PARAM;
			form.appendChild(input);
		}
		input.value = token;
	}

	document.addEventListener('submit', function (e) {
		if (e.target && e.target.tagName === 'FORM') {
			addTokenToForm(e.target);
		}
	}, true);

	document.addEventListener('DOMContentLoaded', function () {
		Array.prototype.forEach.call(document.forms, addTokenToForm);
	});

	var origOpen = XMLHttpRequest.prototype.open;
	var origSend = XMLHttpRequest.prototype.send;
	XMLHttpRequest.prototype.open = function (method, url) {
		this._jakonCsrf = !SAFE_METHODS.test(method) && isSameOrigin(url);
		return origOpen.apply(this, arguments);
	};
	XMLHttpRequest.prototype.send = function () {
		var token = getToken();
		if (this._jakonCsrf && token) {
			this.setRequestHeader(HEADER, token);
		}
		return origSend.apply(this, arguments);
	};

	if (window.fetch) {
		var origFetch = window.fetch;
		window.fetch = function (input, init) {
			var token = getToken();
			var url = typeof input === 'string' ? input : (input && input.url) || String(input);
			var method = (init && init.method) || (input && input.method) || 'GET';
			if (token && !SAFE_METHODS.test(method) && isSameOrigin(url)) {
				init = init || {};
				var headers = new Headers(init.headers || (input instanceof Request ? input.headers : undefined));
				headers.set(HEADER, token);
				init.headers = headers;
			}
			return origFetch.call(this, input, init);
		};
	}
})();
