package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.payments.PaymentStatus

import java.math.BigDecimal
import java.sql.Connection
import java.time.LocalDateTime
import java.util.UUID

/** Helpers creating shop test data directly in the database. */
trait ShopDbSupport {

	protected def withConn[T](f: Connection => T): T = DBHelper.withDbConnection(f)

	protected def unique(prefix: String): String = prefix + "-" + UUID.randomUUID().toString.take(8)

	protected def queryInt(sql: String, params: Any*): Int = withConn(conn => {
		val stmt = conn.prepareStatement(sql)
		params.zipWithIndex.foreach {
			case (v: Int, i) => stmt.setInt(i + 1, v)
			case (v, i) => stmt.setString(i + 1, String.valueOf(v))
		}
		val rs = stmt.executeQuery()
		val res = if (rs.next()) rs.getInt(1) else 0
		stmt.close()
		res
	})

	protected def queryString(sql: String, params: Any*): String = withConn(conn => {
		val stmt = conn.prepareStatement(sql)
		params.zipWithIndex.foreach {
			case (v: Int, i) => stmt.setInt(i + 1, v)
			case (v, i) => stmt.setString(i + 1, String.valueOf(v))
		}
		val rs = stmt.executeQuery()
		val res = if (rs.next()) rs.getString(1) else null
		stmt.close()
		res
	})

	protected def createCategory(name: String = unique("category"), parent: ShopCategory = null): ShopCategory = {
		val c = new ShopCategory
		c.name = name
		c.description = "description"
		c.displayOrder = 1
		c.parentCategory = parent
		c.published = true
		c.create()
		c
	}

	protected def createProduct(price: String = "100", discountPrice: String = null, category: ShopCategory = null): ShopProduct = {
		val p = new ShopProduct
		p.name = unique("product")
		p.description = "description"
		p.shortDescription = "short"
		p.price = new BigDecimal(price)
		p.discountPrice = Option(discountPrice).map(new BigDecimal(_)).orNull
		p.sku = unique("sku")
		p.stockQuantity = 10
		p.category = Option(category).getOrElse(createCategory())
		p.published = true
		p.create()
		p
	}

	protected def createShipping(price: String = "79"): ShippingMethod = {
		val s = new ShippingMethod
		s.name = unique("shipping")
		s.description = "shipping description"
		s.price = new BigDecimal(price)
		s.enabled = true
		s.displayOrder = 10
		s.published = true
		s.create()
		s
	}

	protected def createPayment(gatewayCode: String = "manual", price: String = "0"): PaymentMethod = {
		val p = new PaymentMethod
		p.name = unique("payment")
		p.description = "payment description"
		p.price = new BigDecimal(price)
		p.enabled = true
		p.displayOrder = 10
		p.gatewayCode = gatewayCode
		p.published = true
		p.create()
		p
	}

	protected def createOrder(paymentMethod: PaymentMethod = null, shippingMethod: ShippingMethod = null, customer: Customer = null): ShopOrder = {
		val o = new ShopOrder
		o.orderNumber = unique("ORD")
		o.token = UUID.randomUUID().toString
		o.orderDate = LocalDateTime.now()
		o.status = "NEW"
		o.totalPrice = new BigDecimal("250")
		o.shippingPrice = new BigDecimal("79")
		o.paymentPrice = BigDecimal.ZERO
		o.paymentMethod = paymentMethod
		o.shippingMethod = shippingMethod
		o.customer = customer
		o.guestEmail = "guest@example.com"
		o.billingName = "Jan Novak"
		o.published = true
		o.create()
		o
	}

	protected def createOrderItem(order: ShopOrder, product: ShopProduct = null, quantity: Int = 2): ShopOrderItem = {
		val i = new ShopOrderItem
		i.shopOrder = order
		i.product = product
		i.productName = unique("item")
		i.quantity = quantity
		i.unitPrice = new BigDecimal("50")
		i.totalPrice = new BigDecimal(50 * quantity)
		i.variantSelection = ""
		i.published = true
		i.create()
		i
	}

	protected def createOrderPayment(order: ShopOrder, provider: String, externalId: String, status: String = PaymentStatus.Pending): OrderPayment = {
		val p = new OrderPayment
		p.order = order
		p.provider = provider
		p.externalPaymentId = externalId
		p.status = status
		p.amount = order.totalPrice
		p.currency = "czk"
		p.published = true
		p.create()
		p
	}

	protected def createVariant(name: String = unique("variant")): ProductVariant = {
		val v = new ProductVariant
		v.name = name
		v.objectOrder = 1
		v.published = true
		v.create()
		v
	}

	protected def createVariantValue(variant: ProductVariant, value: String, order: Double = 1): ProductVariantValue = {
		val v = new ProductVariantValue
		v.variant = variant
		v.value = value
		v.objectOrder = order
		v.published = true
		v.create()
		v
	}

	val TestPassword = "password123"

	protected def createAcl(): AclRule = {
		val acl = new AclRule
		acl.name = unique("acl")
		acl.adminAllowed = false
		acl.create()
		acl
	}

	/** Enabled user without admin access, can log in. */
	protected def createPlainUser(): JakonUser = {
		val u = new JakonUser
		u.username = unique("user")
		u.email = u.username + "@example.com"
		u.firstName = "Eva"
		u.lastName = "Plain"
		u.password = TestPassword
		u.enabled = true
		u.acl = createAcl()
		u.create()
		u
	}

	/** Enabled customer without admin access, can log in. */
	protected def createCustomer(): Customer = {
		val c = new Customer
		c.username = unique("customer")
		c.email = c.username + "@example.com"
		c.firstName = "Petr"
		c.lastName = "Svoboda"
		c.password = TestPassword
		c.enabled = true
		c.acl = createAcl()
		c.phone = "123456789"
		c.street = "Ulice 1"
		c.city = "Brno"
		c.zip = "60200"
		c.country = "CZ"
		c.published = true
		c.create()
		c
	}
}
