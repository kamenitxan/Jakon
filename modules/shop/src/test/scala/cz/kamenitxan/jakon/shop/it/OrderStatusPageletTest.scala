package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.payments.{PaymentStatus, TestPaymentGateway}
import org.scalatest.DoNotDiscover

@DoNotDiscover
class OrderStatusPageletTest extends ShopHttpTestBase {

	test("status page without token, with unknown token and with valid token") {
		val browser = new Browser
		assertOk(browser.get("/order/status"))
		assertOk(browser.get("/order/status?token=unknown"))

		val order = createOrder()
		createOrderItem(order)
		val resp = browser.get(s"/order/status?token=${order.token}&paymentError=1")
		assertOk(resp)
		assert(resp.body().contains(order.orderNumber))
	}

	test("status page shows paid order") {
		val order = createOrder()
		createOrderPayment(order, "manual", "", PaymentStatus.Completed)
		assertOk(new Browser().get(s"/order/status?token=${order.token}"))
	}

	test("pay redirects home without token or for unknown order") {
		val browser = new Browser
		assertRedirect(browser.get("/order/pay"), "/")
		assertRedirect(browser.get("/order/pay?token=unknown"), "/")
	}

	test("pay redirects to status for paid order, manual payment and missing payment method") {
		val browser = new Browser
		val paid = createOrder(paymentMethod = createPayment(TestPaymentGateway.Code))
		createOrderPayment(paid, TestPaymentGateway.Code, "x", PaymentStatus.Completed)
		assertRedirect(browser.get(s"/order/pay?token=${paid.token}"), s"/order/status?token=${paid.token}")

		val manual = createOrder(paymentMethod = createPayment("manual"))
		assertRedirect(browser.get(s"/order/pay?token=${manual.token}"), s"/order/status?token=${manual.token}")

		val noMethod = createOrder()
		assertRedirect(browser.get(s"/order/pay?token=${noMethod.token}"), s"/order/status?token=${noMethod.token}")
	}

	test("pay redirects to gateway, or back to status when gateway fails") {
		val browser = new Browser
		val order = createOrder(paymentMethod = createPayment(TestPaymentGateway.Code))
		assertRedirect(browser.get(s"/order/pay?token=${order.token}"), TestPaymentGateway.RedirectBase)

		val stripeOrder = createOrder(paymentMethod = createPayment("stripe"))
		assertRedirect(browser.get(s"/order/pay?token=${stripeOrder.token}"), s"/order/status?token=${stripeOrder.token}")
	}
}
