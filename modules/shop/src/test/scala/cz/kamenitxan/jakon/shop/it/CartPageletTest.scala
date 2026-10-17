package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.payments.TestPaymentGateway
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CartPageletTest extends ShopHttpTestBase {

	private val CartCookie = "cart_token"

	private def cartId(browser: Browser): Int = {
		queryInt("SELECT id FROM Cart WHERE token = ?", browser.cookie(CartCookie).get)
	}

	private def login(browser: Browser, email: String): Unit = {
		browser.post("/admin", "email" -> email, "password" -> TestPassword)
	}

	/** Fills the cart and selects shipping and payment, so the browser is ready to submit the order. */
	private def prepareCheckout(browser: Browser, gatewayCode: String = "manual"): Unit = {
		val product = createProduct(price = "120", discountPrice = "100")
		assertRedirect(browser.post("/cart/add", "productId" -> product.id.toString, "quantity" -> "2"), "/cart")
		val shipping = createShipping()
		val payment = createPayment(gatewayCode, "10")
		assertRedirect(browser.post("/cart/checkout",
			"shippingMethodId" -> shipping.id.toString, "paymentMethodId" -> payment.id.toString), "/cart/delivery")
	}

	private def deliveryParams(email: String = "buyer@example.com", name: String = "Jan Kupec"): Seq[(String, String)] = Seq(
		"guestEmail" -> email,
		"guestPhone" -> "777888999",
		"billingName" -> name,
		"billingStreet" -> "Hlavní 1",
		"billingCity" -> "Praha",
		"billingZip" -> "11000",
		"billingCountry" -> "CZ",
		"customerNote" -> "Prosím rychle"
	)

	test("empty cart page sets cart cookie") {
		val browser = new Browser
		assertOk(browser.get("/cart"))
		assert(browser.cookie(CartCookie).exists(_.nonEmpty))
		// existing cookie is reused
		val token = browser.cookie(CartCookie)
		assertOk(browser.get("/cart"))
		assert(browser.cookie(CartCookie) == token)
	}

	test("add product, show cart and update quantities") {
		val browser = new Browser
		val product = createProduct(price = "250")

		assertRedirect(browser.post("/cart/add", "productId" -> product.id.toString, "quantity" -> "2"), "/cart")
		val cartPage = browser.get("/cart")
		assertOk(cartPage)
		assert(cartPage.body().contains(product.name))

		val variant = createVariant("Barva")
		val red = createVariantValue(variant, "Červená")
		assertRedirect(browser.post("/cart/add", "productId" -> product.id.toString, "variantValues" -> red.id.toString), "/cart")
		val id = cartId(browser)
		assert(queryInt("SELECT COUNT(*) FROM CartItem WHERE cart_id = ?", id) == 2)
		// BUG: ProductVariantService.buildReadableSelection orders by pv.displayOrder, which does not exist
		pendingUntilFixed {
			val variantPage = browser.get("/cart")
			assertOk(variantPage)
			assert(variantPage.body().contains("Červená"))
		}

		val itemId = queryInt("SELECT id FROM CartItem WHERE cart_id = ? AND variantSelection IS NULL", id)
		assertRedirect(browser.post("/cart/update", "itemId" -> itemId.toString, "quantity" -> "4"), "/cart")
		assert(queryInt("SELECT quantity FROM CartItem WHERE id = ?", itemId) == 4)
		assertRedirect(browser.post("/cart/update", "itemId" -> itemId.toString, "quantity" -> "0"), "/cart")
		assert(queryInt("SELECT COUNT(*) FROM CartItem WHERE id = ?", itemId) == 0)
		// invalid values are ignored
		assertRedirect(browser.post("/cart/update", "itemId" -> "abc", "quantity" -> "x"), "/cart")
	}

	test("add to cart without valid product id does nothing") {
		val browser = new Browser
		assertRedirect(browser.post("/cart/add", "productId" -> "not-a-number"), "/cart")
		assertRedirect(browser.post("/cart/add"), "/cart")
		assert(queryInt("SELECT COUNT(*) FROM CartItem WHERE cart_id = ?", cartId(browser)) == 0)
	}

	test("checkout and delivery require items and selected methods") {
		val browser = new Browser
		assertRedirect(browser.get("/cart/checkout"), "/cart")
		assertRedirect(browser.get("/cart/delivery"), "/cart")

		val product = createProduct()
		browser.post("/cart/add", "productId" -> product.id.toString)
		assertOk(browser.get("/cart/checkout"))
		// shipping and payment not selected yet
		assertRedirect(browser.get("/cart/delivery"), "/cart")
		assertRedirect(browser.post("/cart/checkout", "shippingMethodId" -> "x"), "/cart/checkout")

		val shipping = createShipping()
		val payment = createPayment()
		assertRedirect(browser.post("/cart/checkout",
			"shippingMethodId" -> shipping.id.toString, "paymentMethodId" -> payment.id.toString), "/cart/delivery")
		assertOk(browser.get("/cart/delivery"))
	}

	test("submit order validates cart state and delivery data") {
		val emptyBrowser = new Browser
		assertRedirect(emptyBrowser.post("/cart/submit", deliveryParams()*), "/cart")

		val noMethods = new Browser
		noMethods.post("/cart/add", "productId" -> createProduct().id.toString)
		assertRedirect(noMethods.post("/cart/submit", deliveryParams()*), "/cart/checkout")

		val missingData = new Browser
		prepareCheckout(missingData)
		assertRedirect(missingData.post("/cart/submit", "guestEmail" -> ""), "/cart/delivery")
		assertRedirect(missingData.post("/cart/submit", "guestEmail" -> "a@example.com"), "/cart/delivery")
	}

	test("submit order with manual payment creates order, items and email") {
		val browser = new Browser
		prepareCheckout(browser)
		val email = unique("buyer") + "@example.com"
		val resp = browser.post("/cart/submit", deliveryParams(email)*)
		assertRedirect(resp, "/cart/success?orderNumber=")

		val orderNumber = location(resp).split("orderNumber=")(1)
		val orderId = queryInt("SELECT id FROM ShopOrder WHERE orderNumber = ?", orderNumber)
		assert(orderId > 0)
		assert(queryString("SELECT guestEmail FROM ShopOrder WHERE id = ?", orderId) == email)
		// delivery address falls back to billing address
		assert(queryString("SELECT deliveryCity FROM ShopOrder WHERE id = ?", orderId) == "Praha")
		assert(queryInt("SELECT COUNT(*) FROM ShopOrderItem WHERE order_id = ?", orderId) == 1)
		// 2 x 100 (discount) + 79 shipping + 10 payment
		assert(new java.math.BigDecimal(queryString("SELECT totalPrice FROM ShopOrder WHERE id = ?", orderId)).compareTo(new java.math.BigDecimal("289")) == 0)
		assert(queryInt("SELECT COUNT(*) FROM EmailEntity WHERE addressTo = ?", email) == 1)
		// cart is deleted
		assert(queryInt("SELECT COUNT(*) FROM Cart WHERE token = ?", browser.cookie(CartCookie).getOrElse("")) == 0)

		val success = browser.get(location(resp))
		assertOk(success)
		assert(success.body().contains(orderNumber))
	}

	test("submit order with redirect gateway redirects to payment page") {
		val browser = new Browser
		prepareCheckout(browser, TestPaymentGateway.Code)
		val resp = browser.post("/cart/submit", deliveryParams()*)
		assertRedirect(resp, TestPaymentGateway.RedirectBase)
	}

	test("submit order as logged in customer links the order to customer") {
		// BUG: Customer can not be created, see ShopEntityTest "Customer create and update"
		pendingUntilFixed {
			val customer = createCustomer()
			val browser = new Browser
			login(browser, customer.email)
			prepareCheckout(browser)
			val delivery = browser.get("/cart/delivery")
			assertOk(delivery)
			assert(delivery.body().contains(customer.email))

			val resp = browser.post("/cart/submit", deliveryParams(customer.email)*)
			val orderNumber = location(resp).split("orderNumber=")(1)
			assert(queryInt("SELECT customer_id FROM ShopOrder WHERE orderNumber = ?", orderNumber) == customer.id)
		}
	}

	test("delivery form is prefilled for logged in non-customer user") {
		val user = createPlainUser()
		val browser = new Browser
		login(browser, user.email)
		prepareCheckout(browser)
		val delivery = browser.get("/cart/delivery")
		assertOk(delivery)
		assert(delivery.body().contains(user.email))
	}

	test("success page without or with unknown order number") {
		val browser = new Browser
		assertOk(browser.get("/cart/success"))
		assertOk(browser.get("/cart/success?orderNumber=UNKNOWN"))
	}
}
