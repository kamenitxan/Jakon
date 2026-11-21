package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.payments.PaymentStatus
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

/** Create / update round trips of all shop entities, with and without their relations. */
@DoNotDiscover
class ShopEntityTest extends AnyFunSuite with ShopDbSupport {

	test("ShopCategory create and update with and without parent") {
		val parent = createCategory()
		val child = createCategory(parent = parent)
		assert(queryInt("SELECT parentCategory_id FROM ShopCategory WHERE id = ?", child.id) == parent.id)

		child.parentCategory = null
		child.name = "renamed category"
		child.update()
		assert(queryString("SELECT name FROM ShopCategory WHERE id = ?", child.id) == "renamed category")
		assert(queryInt("SELECT COUNT(*) FROM ShopCategory WHERE id = ? AND parentCategory_id IS NULL", child.id) == 1)

		parent.parentCategory = child
		parent.update()
		assert(queryInt("SELECT parentCategory_id FROM ShopCategory WHERE id = ?", parent.id) == child.id)

		assert(child.toString.contains("renamed category"))
		assert(child.createUrl.nonEmpty)
	}

	test("ShopProduct create") {
		val category = createCategory()
		val product = createProduct(price = "200", discountPrice = "150", category = category)
		assert(queryInt("SELECT category_id FROM ShopProduct WHERE id = ?", product.id) == category.id)
		assert(product.toString.contains(product.name))
	}

	test("ShopProduct update with and without discount") {
		val product = createProduct(price = "200", discountPrice = "150")
		product.name = "updated product"
		product.discountPrice = null
		product.featured = true
		product.update()
		assert(queryString("SELECT name FROM ShopProduct WHERE id = ?", product.id) == "updated product")
		assert(queryInt("SELECT COUNT(*) FROM ShopProduct WHERE id = ? AND discountPrice IS NULL", product.id) == 1)

		product.discountPrice = new BigDecimal("99")
		product.update()
		assert(queryString("SELECT discountPrice FROM ShopProduct WHERE id = ?", product.id) == "99")
	}

	test("ShopProduct variant references main product and has own variant values") {
		val main = createProduct(price = "100")
		val size = createVariant("Velikost")
		val xl = createVariantValue(size, "XL")
		val variant = createProductVariant(main, price = "130", values = Seq(xl))

		assert(queryInt("SELECT mainProduct_id FROM ShopProduct WHERE id = ?", variant.id) == main.id)
		assert(queryInt("SELECT COUNT(*) FROM ShopProduct WHERE id = ? AND mainProduct_id IS NULL", main.id) == 1)
		assert(queryInt("SELECT productVariantValue_id FROM ShopProductProductVariantValue WHERE shopProduct_id = ?", variant.id) == xl.id)
		assert(variant.variantGroupId == main.id)
		assert(main.variantGroupId == main.id)

		// variant can be detached from its main product
		variant.mainProduct = null
		variant.update()
		assert(queryInt("SELECT COUNT(*) FROM ShopProduct WHERE id = ? AND mainProduct_id IS NULL", variant.id) == 1)
	}

	test("ShippingMethod and PaymentMethod create and update") {
		val shipping = createShipping()
		shipping.name = "updated shipping"
		shipping.estimatedDeliveryDays = 3
		shipping.update()
		assert(queryString("SELECT name FROM ShippingMethod WHERE id = ?", shipping.id) == "updated shipping")
		assert(shipping.toString.contains("updated shipping"))

		val payment = createPayment()
		payment.name = "updated payment"
		payment.gatewayCode = "stripe"
		payment.update()
		assert(queryString("SELECT gatewayCode FROM PaymentMethod WHERE id = ?", payment.id) == "stripe")
		assert(payment.toString.contains("updated payment"))
	}

	test("Cart create and update with and without selected methods") {
		val shipping = createShipping()
		val payment = createPayment()

		val cart = new Cart
		cart.token = unique("token")
		cart.selectedShippingMethod = shipping
		cart.selectedPaymentMethod = payment
		cart.published = true
		cart.create()
		assert(queryInt("SELECT selectedShippingMethod_id FROM Cart WHERE id = ?", cart.id) == shipping.id)

		cart.selectedShippingMethod = null
		cart.selectedPaymentMethod = null
		cart.update()
		assert(queryInt("SELECT COUNT(*) FROM Cart WHERE id = ? AND selectedShippingMethod_id IS NULL AND selectedPaymentMethod_id IS NULL", cart.id) == 1)

		cart.selectedShippingMethod = shipping
		cart.selectedPaymentMethod = payment
		cart.update()
		assert(queryInt("SELECT selectedPaymentMethod_id FROM Cart WHERE id = ?", cart.id) == payment.id)

		val emptyCart = new Cart
		emptyCart.token = unique("token")
		emptyCart.create()
		assert(queryInt("SELECT COUNT(*) FROM Cart WHERE id = ? AND selectedShippingMethod_id IS NULL", emptyCart.id) == 1)
		assert(cart.toString.contains(cart.token))
	}

	test("CartItem create and update") {
		val cart = new Cart
		cart.token = unique("token")
		cart.create()
		val product = createProduct()

		val item = new CartItem
		item.cart = cart
		item.product = product
		item.quantity = 2
		item.create()
		item.quantity = 5
		item.update()
		assert(queryInt("SELECT quantity FROM CartItem WHERE id = ?", item.id) == 5)
		assert(item.toString.nonEmpty)
	}

	test("ShopOrder create and update with and without payment and shipping method") {
		val payment = createPayment()
		val shipping = createShipping()
		val order = createOrder(payment, shipping)
		assert(queryInt("SELECT paymentMethod_id FROM ShopOrder WHERE id = ?", order.id) == payment.id)
		assert(queryInt("SELECT COUNT(*) FROM ShopOrder WHERE id = ? AND customer_id IS NULL", order.id) == 1)

		order.paymentMethod = null
		order.shippingMethod = null
		order.status = "SHIPPED"
		order.update()
		assert(queryString("SELECT status FROM ShopOrder WHERE id = ?", order.id) == "SHIPPED")
		assert(queryInt("SELECT COUNT(*) FROM ShopOrder WHERE id = ? AND paymentMethod_id IS NULL AND shippingMethod_id IS NULL", order.id) == 1)

		order.paymentMethod = payment
		order.shippingMethod = shipping
		order.update()
		assert(queryInt("SELECT shippingMethod_id FROM ShopOrder WHERE id = ?", order.id) == shipping.id)
		assert(order.toString.contains(order.orderNumber))
	}

	test("ShopOrder with customer") {
		val customer = createCustomer()
		val order = createOrder(customer = customer)
		assert(queryInt("SELECT customer_id FROM ShopOrder WHERE id = ?", order.id) == customer.id)
		order.customer = null
		order.update()
		assert(queryInt("SELECT COUNT(*) FROM ShopOrder WHERE id = ? AND customer_id IS NULL", order.id) == 1)
	}

	test("ShopOrderItem create and update with and without product") {
		val order = createOrder()
		val product = createProduct()
		val item = createOrderItem(order, product)
		assert(queryInt("SELECT product_id FROM ShopOrderItem WHERE id = ?", item.id) == product.id)

		item.product = null
		item.note = "updated note"
		item.update()
		assert(queryInt("SELECT COUNT(*) FROM ShopOrderItem WHERE id = ? AND product_id IS NULL", item.id) == 1)

		item.product = product
		item.update()
		assert(queryInt("SELECT product_id FROM ShopOrderItem WHERE id = ?", item.id) == product.id)

		val noProductItem = createOrderItem(order)
		assert(queryInt("SELECT COUNT(*) FROM ShopOrderItem WHERE id = ? AND product_id IS NULL", noProductItem.id) == 1)
		assert(item.toString.contains(item.productName))
	}

	test("OrderPayment create and update") {
		val order = createOrder()
		val payment = createOrderPayment(order, "manual", "ext-1")
		assert(queryInt("SELECT order_id FROM OrderPayment WHERE id = ?", payment.id) == order.id)

		payment.status = PaymentStatus.Completed
		payment.update()
		assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", payment.id) == PaymentStatus.Completed)
		assert(payment.toString.nonEmpty)
	}

	test("OrderReturn and OrderReturnItem create and update") {
		val order = createOrder()
		val orderItem = createOrderItem(order)

		val ret = new OrderReturn
		ret.order = order
		ret.returnNumber = unique("RET")
		ret.reason = OrderReturnReason.Defective
		ret.bankAccount = "123/0100"
		ret.published = true
		ret.create()
		ret.status = OrderReturnStatus.Approved
		ret.update()
		assert(queryString("SELECT status FROM OrderReturn WHERE id = ?", ret.id) == OrderReturnStatus.Approved)
		assert(queryInt("SELECT order_id FROM OrderReturn WHERE id = ?", ret.id) == order.id)

		val retItem = new OrderReturnItem
		retItem.orderReturn = ret
		retItem.orderItem = orderItem
		retItem.productName = orderItem.productName
		retItem.quantity = 1
		retItem.create()
		retItem.quantity = 2
		retItem.update()
		assert(queryInt("SELECT quantity FROM OrderReturnItem WHERE id = ?", retItem.id) == 2)
		assert(queryInt("SELECT orderReturn_id FROM OrderReturnItem WHERE id = ?", retItem.id) == ret.id)

		assert(ret.toString.nonEmpty)
		assert(retItem.toString.nonEmpty)
		assert(OrderReturnReason.all.nonEmpty)
	}

	test("ProductVariant and ProductVariantValue create and update") {
		val variant = createVariant()
		variant.name = "updated variant"
		variant.update()
		assert(queryString("SELECT name FROM ProductVariant WHERE id = ?", variant.id) == "updated variant")

		val value = createVariantValue(variant, "red")
		value.value = "blue"
		value.update()
		assert(queryString("SELECT value FROM ProductVariantValue WHERE id = ?", value.id) == "blue")

		value.variant = null
		assert(value.toString.contains("?"))
		value.variant = variant
		assert(value.toString.contains("updated variant"))
		assert(variant.toString.contains("updated variant"))
	}

	test("Customer create and update") {
		val customer = createCustomer()
		assert(queryString("SELECT city FROM Customer WHERE id = ?", customer.id) == "Brno")
		customer.city = "Ostrava"
		customer.deliveryCity = "Olomouc"
		customer.update()
		assert(queryString("SELECT city FROM Customer WHERE id = ?", customer.id) == "Ostrava")
		assert(customer.toString.nonEmpty)
	}
}
