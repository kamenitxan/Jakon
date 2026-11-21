package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.payments.{PaymentGatewayException, PaymentService, PaymentStatus, TestPaymentGateway}
import cz.kamenitxan.jakon.shop.service.*
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import scala.jdk.CollectionConverters.*

@DoNotDiscover
class ShopServiceDbTest extends AnyFunSuite with ShopDbSupport {

	private val successUrl = "https://shop.example.com/ok"
	private val cancelUrl = "https://shop.example.com/cancel"

	test("buildReadableVariant describes product by its variant values ordered by variant") {
		// order of variants and values is assigned by Ordered on creation
		val color = createVariant("Barva")
		val size = createVariant("Velikost")
		val xl = createVariantValue(size, "XL")
		val red = createVariantValue(color, "Červená")
		val main = createProduct()
		val variant = createProductVariant(main, price = "100", values = Seq(xl, red))

		withConn(implicit conn => {
			assert(ProductVariantService.buildReadableVariant(variant.id) == "Barva: Červená, Velikost: XL")
			assert(ProductVariantService.buildReadableVariant(main.id) == "")
		})
	}

	test("loadVariantGroup returns published main product and variants") {
		val main = createProduct(price = "100")
		val v1 = createProductVariant(main, price = "110")
		val v2 = createProductVariant(main, price = "120")
		val hidden = createProductVariant(main, price = "130")
		hidden.published = false
		hidden.update()
		val standalone = createProduct()

		withConn(implicit conn => {
			val expected = Seq(main.id, v1.id, v2.id)
			assert(ProductVariantService.loadVariantGroup(main).map(_.id) == expected)
			assert(ProductVariantService.loadVariantGroup(v2).map(_.id) == expected)
			assert(ProductVariantService.loadVariantGroup(standalone).map(_.id) == Seq(standalone.id))
		})
	}

	test("buildSelector creates axes with selected values and options with variant prices") {
		val color = createVariant("Barva")
		val size = createVariant("Velikost")
		val red = createVariantValue(color, "Červená")
		val blue = createVariantValue(color, "Modrá")
		val m = createVariantValue(size, "M")
		val xl = createVariantValue(size, "XL")

		val main = createProduct(price = "100")
		setVariantValues(main, red, m)
		val redXl = createProductVariant(main, price = "130", discountPrice = "120", values = Seq(red, xl))
		val blueM = createProductVariant(main, price = "110", values = Seq(blue, m))

		withConn(implicit conn => {
			val group = ProductVariantService.loadVariantGroup(main)
			val selector = ProductVariantService.buildSelector(group.find(_.id == redXl.id).get, group).get

			val axes = selector.axes.asScala.toSeq
			assert(axes.map(_.variant.name) == Seq("Barva", "Velikost"))
			assert(axes.head.values.asScala.map(_.value.value).toSeq == Seq("Červená", "Modrá"))
			assert(axes.flatMap(_.values.asScala).filter(_.selected).map(_.value.id).toSet == Set(red.id, xl.id))

			val options = selector.options.map(o => o.productId -> o).toMap
			assert(options.keySet == Set(main.id, redXl.id, blueM.id))
			assert(options(redXl.id).valueIds.toSeq == Seq(red.id, xl.id).sorted)
			assert(options(redXl.id).discountPrice.compareTo(new java.math.BigDecimal("120")) == 0)
			assert(options(blueM.id).price.compareTo(new java.math.BigDecimal("110")) == 0)

			// product without variants has no selector
			val standalone = createProduct()
			assert(ProductVariantService.buildSelector(standalone, Seq(standalone)).isEmpty)
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
