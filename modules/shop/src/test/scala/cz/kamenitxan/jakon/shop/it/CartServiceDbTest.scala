package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.service.CartService
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

@DoNotDiscover
class CartServiceDbTest extends AnyFunSuite with ShopDbSupport {

	test("getOrCreateCart creates a cart once and then returns the existing one") {
		withConn(implicit conn => {
			val token = unique("token")
			assert(CartService.findCartByToken(token).isEmpty)
			val cart = CartService.getOrCreateCart(token)
			assert(cart.id > 0)
			assert(CartService.getOrCreateCart(token).id == cart.id)
			assert(CartService.findCartByToken(token).map(_.id).contains(cart.id))
		})
	}

	test("addItem merges quantity of the same product, variants are separate items with own price") {
		withConn(implicit conn => {
			val cart = CartService.getOrCreateCart(unique("token"))
			val product = createProduct(price = "100")
			CartService.addItem(cart.id, product.id, 1)
			val merged = CartService.addItem(cart.id, product.id, 2)
			assert(merged.quantity == 3)

			val variant = createProductVariant(product, price = "150", discountPrice = "120")
			CartService.addItem(cart.id, variant.id, 1)
			val mergedVariant = CartService.addItem(cart.id, variant.id, 4)
			assert(mergedVariant.quantity == 5)

			val items = CartService.getItems(cart.id)
			assert(items.size == 2)
			// 3 x 100 + 5 x 120 (variant discount price)
			assert(CartService.getTotalPrice(items).compareTo(new BigDecimal("900")) == 0)
		})
	}

	test("addItem throws for unknown product") {
		withConn(implicit conn => {
			val cart = CartService.getOrCreateCart(unique("token"))
			intercept[IllegalArgumentException] {
				CartService.addItem(cart.id, 999999, 1)
			}
		})
	}

	test("updateItemQuantity changes quantity and removes item when quantity is zero") {
		withConn(implicit conn => {
			val cart = CartService.getOrCreateCart(unique("token"))
			val item = CartService.addItem(cart.id, createProduct().id, 1)
			CartService.updateItemQuantity(item.id, 7)
			assert(CartService.getItems(cart.id).head.quantity == 7)
			CartService.updateItemQuantity(item.id, 0)
			assert(CartService.getItems(cart.id).isEmpty)
		})
	}

	test("saveShippingAndPayment, clearCart and deleteCart") {
		withConn(implicit conn => {
			val token = unique("token")
			val cart = CartService.getOrCreateCart(token)
			val shipping = createShipping()
			val payment = createPayment()
			CartService.addItem(cart.id, createProduct().id, 1)
			CartService.saveShippingAndPayment(cart.id, shipping.id, payment.id)
			assert(queryInt("SELECT selectedShippingMethod_id FROM Cart WHERE id = ?", cart.id) == shipping.id)
			assert(queryInt("SELECT selectedPaymentMethod_id FROM Cart WHERE id = ?", cart.id) == payment.id)

			CartService.clearCart(cart.id)
			assert(CartService.getItems(cart.id).isEmpty)

			CartService.addItem(cart.id, createProduct().id, 1)
			CartService.deleteCart(cart.id)
			assert(CartService.findCartByToken(token).isEmpty)
			assert(queryInt("SELECT COUNT(*) FROM CartItem WHERE cart_id = ?", cart.id) == 0)
		})
	}
}
