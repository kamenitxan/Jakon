package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.payments.{PaymentGatewayException, PaymentService, PaymentStatus, TestPaymentGateway}
import cz.kamenitxan.jakon.shop.service.*
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

@DoNotDiscover
class ShopServiceDbTest extends AnyFunSuite with ShopDbSupport {

	private val successUrl = "https://shop.example.com/ok"
	private val cancelUrl = "https://shop.example.com/cancel"

	test("ProductVariantService loads published values ordered and builds readable selection") {
		withConn(implicit conn => {
			val variant = createVariant("Barva")
			// order of values is assigned by Ordered on creation
			val red = createVariantValue(variant, "Červená")
			val blue = createVariantValue(variant, "Modrá")
			val hidden = createVariantValue(variant, "Skrytá")
			hidden.published = false
			hidden.update()

			val values = ProductVariantService.loadValuesForVariant(variant.id)
			assert(values.map(_.value) == Seq("Červená", "Modrá"))
			assert(VariantWithValues(variant, values).values.size == 2)

			assert(ProductVariantService.buildReadableSelection(null) == "")
			assert(ProductVariantService.buildReadableSelection("  ") == "")
			assert(ProductVariantService.buildReadableSelection("abc,def") == "")
			// BUG: buildReadableSelection orders by pv.displayOrder, but ProductVariant has only objectOrder
			pendingUntilFixed {
				assert(ProductVariantService.buildReadableSelection(s"${red.id}, ${blue.id}") == "Barva: Červená, Barva: Modrá")
			}
		})
	}

	test("OrderItemService returns items of order") {
		withConn(implicit conn => {
			val order = createOrder()
			createOrderItem(order)
			createOrderItem(order)
			assert(OrderItemService.getByOrder(order.id).size == 2)
			assert(OrderItemService.getByOrder(createOrder().id).isEmpty)
		})
	}

	test("OrderPaymentService create, query, update and refresh status") {
		withConn(implicit conn => {
			val order = createOrder()
			assert(OrderPaymentService.getLatestByOrder(order.id).isEmpty)

			val init = TestPaymentGateway.initializePayment(PaymentService.createPaymentRequest(order, successUrl, cancelUrl, "czk"))
			val created = OrderPaymentService.create(order, init)
			assert(created.status == PaymentStatus.Pending)
			assert(created.currency == "czk")

			val second = createOrderPayment(order, TestPaymentGateway.Code, "completed-1")
			assert(OrderPaymentService.getByOrder(order.id).size == 2)
			assert(OrderPaymentService.getLatestByOrder(order.id).map(_.id).contains(second.id))

			// gateway reports completed -> status is updated
			assert(OrderPaymentService.refreshStatus(second) == PaymentStatus.Completed)
			assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", second.id) == PaymentStatus.Completed)

			// gateway without remote status -> unchanged
			assert(OrderPaymentService.refreshStatus(created) == PaymentStatus.Pending)
			// unknown gateway -> unchanged
			val unknown = createOrderPayment(order, "unknown-gateway", "x")
			assert(OrderPaymentService.refreshStatus(unknown) == PaymentStatus.Pending)

			OrderPaymentService.updateStatus(created.id, PaymentStatus.Cancelled)
			assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", created.id) == PaymentStatus.Cancelled)
		})
	}

	test("PaymentService initializes payment through redirect gateway") {
		withConn(implicit conn => {
			val order = createOrder(paymentMethod = createPayment(TestPaymentGateway.Code, "19"), shippingMethod = createShipping())
			createOrderItem(order)
			val url = PaymentService.gatewayRedirectUrl(order, successUrl, cancelUrl)
			assert(url.contains(TestPaymentGateway.RedirectBase + "session-" + order.orderNumber))
			assert(OrderPaymentService.getByOrder(order.id).exists(_.provider == TestPaymentGateway.Code))
		})
	}

	test("PaymentService returns no redirect for manual, unconfigured or unsupported gateways") {
		withConn(implicit conn => {
			val manualOrder = createOrder(paymentMethod = createPayment("manual"))
			assert(PaymentService.gatewayRedirectUrl(manualOrder, successUrl, cancelUrl).isEmpty)

			val stripeOrder = createOrder(paymentMethod = createPayment("stripe"))
			assert(PaymentService.gatewayRedirectUrl(stripeOrder, successUrl, cancelUrl).isEmpty)
			intercept[PaymentGatewayException] {
				PaymentService.initializePayment(stripeOrder, successUrl, cancelUrl)
			}

			val unknownOrder = createOrder(paymentMethod = createPayment("unknown-gateway"))
			assert(PaymentService.gatewayRedirectUrl(unknownOrder, successUrl, cancelUrl).isEmpty)
		})
	}

	test("PaymentService createPaymentRequest loads order items from database") {
		withConn(implicit conn => {
			val order = createOrder()
			createOrderItem(order, quantity = 3)
			val request = PaymentService.createPaymentRequest(order, successUrl, cancelUrl)
			assert(request.lineItems.exists(_.quantity == 3))
		})
	}

	test("ShopCategoryService, ShopProductService and ShopUtils load seeded data") {
		withConn(implicit conn => {
			assert(ShopCategoryService.getAll().nonEmpty)
			assert(ShopProductService.getAll().nonEmpty)
		})
		assert(ShopUtils.categoryTree().forall(_.parentCategory == null))
		assert(ShopUtils.commonPageData.contains("categories"))
	}
}
