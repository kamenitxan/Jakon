package cz.kamenitxan.jakon.shop.it

import org.scalatest.DoNotDiscover

@DoNotDiscover
class CartSummaryJsonPageletTest extends ShopHttpTestBase {

	/** JSON body without whitespace */
	private def compact(body: String): String = body.replaceAll("\\s", "")

	test("summary without cart cookie returns empty cart") {
		val resp = new Browser().get("/api/cart/summary")
		assertOk(resp)
		assert(compact(resp.body()).contains("\"totalQuantity\":0"))
	}

	test("summary returns totals of the current cart") {
		val browser = new Browser
		val product = createProduct(price = "150")
		browser.post("/cart/add", "productId" -> product.id.toString, "quantity" -> "3")
		val resp = browser.get("/api/cart/summary")
		assertOk(resp)
		val body = compact(resp.body())
		assert(body.contains("\"totalQuantity\":3"))
		assert(body.contains("\"itemCount\":1"))
		assert(body.contains("450.00Kč"))
	}
}
