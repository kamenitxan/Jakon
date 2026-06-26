(function () {
	'use strict';

	function getCookie(name) {
		var match = document.cookie.match('(?:^|; )' + name + '=([^;]*)');
		return match ? decodeURIComponent(match[1]) : null;
	}

	function updateCartSummary() {
		if (!getCookie('cart_token')) {
			return;
		}
		fetch('/api/cart/summary')
			.then(function (res) { return res.json(); })
			.then(function (json) {
				var data = json.data !== undefined ? json.data : json;
				var totalQuantity = data.totalQuantity || 0;
				var itemCount = data.itemCount || 0;
				var subtotalFormatted = data.subtotalFormatted || '0';

				var badge = document.getElementById('cart-badge');
				var itemCountEl = document.getElementById('cart-item-count');
				var subtotalEl = document.getElementById('cart-subtotal');

				if (badge) {
					badge.textContent = totalQuantity;
					badge.style.display = totalQuantity > 0 ? '' : 'none';
				}
				if (itemCountEl) {
					itemCountEl.textContent = itemCount;
				}
				if (subtotalEl) {
					subtotalEl.textContent = subtotalFormatted;
				}
			})
			.catch(function () {
				// silently ignore — cart badge stays hidden
			});
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', updateCartSummary);
	} else {
		updateCartSummary();
	}
})();
