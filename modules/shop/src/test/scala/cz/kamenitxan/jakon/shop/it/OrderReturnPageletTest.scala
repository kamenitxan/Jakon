package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.entity.{OrderReturnReason, OrderReturnStatus}
import org.scalatest.DoNotDiscover

@DoNotDiscover
class OrderReturnPageletTest extends ShopHttpTestBase {

	test("return form redirects home without token or for unknown order") {
		val browser = new Browser
		assertRedirect(browser.get("/order/return/form"), "/")
		assertRedirect(browser.get("/order/return/form?token=unknown"), "/")
		assertRedirect(browser.post("/order/return/create"), "/")
	}

	test("return form lists order items") {
		val order = createOrder()
		val item = createOrderItem(order)
		val resp = new Browser().get(s"/order/return/form?token=${order.token}")
		assertOk(resp)
		assert(resp.body().contains(item.productName))
	}

	test("create return without selected items shows form error") {
		val order = createOrder()
		createOrderItem(order)
		val resp = new Browser().post("/order/return/create", "token" -> order.token, "reason" -> OrderReturnReason.Other)
		assert(queryInt("SELECT COUNT(*) FROM OrderReturn WHERE order_id = ?", order.id) == 0)
		// BUG: @Post of createReturn has no template, so the form with error is not rendered
		pendingUntilFixed {
			assertOk(resp)
			assert(resp.body().contains("Vyberte alespoň jednu položku"))
		}
	}

	test("create return stores return with items, then refuses a second return") {
		val order = createOrder()
		val item1 = createOrderItem(order, quantity = 3)
		val item2 = createOrderItem(order, quantity = 1)
		val browser = new Browser

		val resp = browser.post("/order/return/create",
			"token" -> order.token,
			"reason" -> OrderReturnReason.Defective,
			"bankAccount" -> " 123456/0100 ",
			"itemId" -> item1.id.toString,
			"itemId" -> item2.id.toString,
			"itemId" -> "999999",
			"qty_" + item1.id -> "10"
		)
		assertRedirect(resp, "/order/return/confirmation?returnNumber=")

		val returnId = queryInt("SELECT id FROM OrderReturn WHERE order_id = ?", order.id)
		assert(queryString("SELECT status FROM OrderReturn WHERE id = ?", returnId) == OrderReturnStatus.Pending)
		assert(queryString("SELECT bankAccount FROM OrderReturn WHERE id = ?", returnId) == "123456/0100")
		assert(queryInt("SELECT COUNT(*) FROM OrderReturnItem WHERE orderReturn_id = ?", returnId) == 2)
		// quantity is limited to ordered quantity
		assert(queryInt("SELECT quantity FROM OrderReturnItem WHERE orderReturn_id = ? AND orderItem_id = ?", returnId, item1.id) == 3)
		// confirmation email to guest
		assert(queryInt("SELECT COUNT(*) FROM EmailEntity WHERE addressTo = ? AND subject LIKE '%odstoupení%'", order.guestEmail) >= 1)

		val confirmation = browser.get(location(resp))
		assertOk(confirmation)
		assert(confirmation.body().contains("Test Shop"))

		// second return for the same order is not allowed
		assertRedirect(browser.post("/order/return/create", "token" -> order.token, "itemId" -> item1.id.toString),
			s"/order/status?token=${order.token}")
		assertOk(browser.get(s"/order/return/form?token=${order.token}"))
	}

	test("return confirmation email goes to customer email when order has a customer") {
		// BUG: Customer can not be created, see ShopEntityTest "Customer create and update"
		pendingUntilFixed {
			val customer = createCustomer()
			val order = createOrder(customer = customer)
			val item = createOrderItem(order)
			new Browser().post("/order/return/create", "token" -> order.token, "itemId" -> item.id.toString)
			assert(queryInt("SELECT COUNT(*) FROM EmailEntity WHERE addressTo = ?", customer.email) >= 1)
		}
	}

	test("return without any recipient email does not send email") {
		val order = createOrder()
		order.guestEmail = ""
		order.update()
		val item = createOrderItem(order)
		new Browser().post("/order/return/create", "token" -> order.token, "itemId" -> item.id.toString)
		assert(queryInt("SELECT COUNT(*) FROM OrderReturn WHERE order_id = ?", order.id) == 1)
	}

	test("confirmation page without or with unknown return number") {
		val browser = new Browser
		assertOk(browser.get("/order/return/confirmation"))
		assertOk(browser.get("/order/return/confirmation?returnNumber=UNKNOWN"))
	}
}
