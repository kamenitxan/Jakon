package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.shop.entity.{Cart, CartItem, ShopProduct}
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

class CartServiceTotalPriceTest extends AnyFunSuite {

	private def item(price: BigDecimal, discountPrice: BigDecimal, quantity: Int): CartItem = {
		val product = new ShopProduct()
		product.price = price
		product.discountPrice = discountPrice
		val cartItem = new CartItem()
		cartItem.product = product
		cartItem.quantity = quantity
		cartItem
	}

	test("getTotalPrice returns zero for an empty list of items") {
		assert(CartService.getTotalPrice(Seq.empty).compareTo(BigDecimal.ZERO) == 0)
	}

	test("getTotalPrice multiplies the regular price by quantity when there is no discount") {
		val items = Seq(item(new BigDecimal("50.00"), null, 3))
		assert(CartService.getTotalPrice(items).compareTo(new BigDecimal("150.00")) == 0)
	}

	test("getTotalPrice prefers the discount price over the regular price") {
		val items = Seq(item(new BigDecimal("50.00"), new BigDecimal("40.00"), 2))
		assert(CartService.getTotalPrice(items).compareTo(new BigDecimal("80.00")) == 0)
	}

	test("getTotalPrice sums multiple cart items") {
		val items = Seq(
			item(new BigDecimal("50.00"), null, 2),
			item(new BigDecimal("30.00"), new BigDecimal("25.00"), 1)
		)
		assert(CartService.getTotalPrice(items).compareTo(new BigDecimal("125.00")) == 0)
	}

	test("getTotalPrice treats an item with no product as zero") {
		val cartItem = new CartItem()
		cartItem.product = null
		cartItem.quantity = 5
		assert(CartService.getTotalPrice(Seq(cartItem)).compareTo(BigDecimal.ZERO) == 0)
	}
}
