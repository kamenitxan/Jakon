package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.entity.{Cart, CartItem, ShopProduct}

import java.math.BigDecimal
import java.sql.Connection
import java.time.LocalDateTime

object CartService {

	implicit val cartCls: Class[Cart] = classOf[Cart]

	def findCartByToken(token: String)(implicit conn: Connection): Option[Cart] = {
		val sql = "SELECT * FROM Cart WHERE token = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, token)
		Option(DBHelper.selectSingleDeep(stmt))
	}

	def getOrCreateCart(token: String)(implicit conn: Connection): Cart = {
		val sql = "SELECT * FROM Cart WHERE token = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, token)
		val existingCart = DBHelper.selectSingleDeep(stmt)
		if (existingCart == null) {
			val cart = new Cart
			cart.token = token
			cart.createdAt = LocalDateTime.now()
			cart.updatedAt = LocalDateTime.now()
			cart.published = true
			cart.create()
			Logger.debug(s"Created new cart with token=$token id=${cart.id}")
			cart
		} else {
			existingCart
		}

	}

	def addItem(cartId: Int, productId: Int, quantity: Int, variantSelection: String = null)(implicit conn: Connection): CartItem = {
		val existing = findItem(cartId, productId, variantSelection)
		existing match {
			case Some(item) =>
				val newQty = item.quantity + quantity
				updateItemQuantity(item.id, newQty)
				item.quantity = newQty
				item
			case None =>
				// Verify product exists without JakonObject JOIN (ShopProduct is standAlone)
				val pstmt = conn.prepareStatement("SELECT id FROM ShopProduct WHERE id = ?")
				pstmt.setInt(1, productId)
				val rs = pstmt.executeQuery()
				val productExists = rs.next()
				rs.close()
				pstmt.close()
				if (!productExists) throw new IllegalArgumentException(s"Product $productId not found")

				val item = new CartItem
				item.cart = new Cart
				item.cart.id = cartId
				item.product = new ShopProduct
				item.product.id = productId
				item.quantity = quantity
				item.variantSelection = variantSelection
				item.published = true
				item.create()
				Logger.debug(s"Added CartItem: product=$productId qty=$quantity variants=$variantSelection to cart=$cartId")
				item
		}
	}

	private def findItem(cartId: Int, productId: Int, variantSelection: String)(implicit conn: Connection): Option[CartItem] = {
		// Items with the same product but different variant selection are treated as separate cart entries
		val (sql, params) = if (variantSelection == null || variantSelection.isEmpty) {
			("SELECT * FROM CartItem WHERE cart_id = ? AND product_id = ? AND (variantSelection IS NULL OR variantSelection = '')",
				Seq[Any](cartId, productId))
		} else {
			("SELECT * FROM CartItem WHERE cart_id = ? AND product_id = ? AND variantSelection = ?",
				Seq[Any](cartId, productId, variantSelection))
		}
		val stmt = conn.prepareStatement(sql)
		params.zipWithIndex.foreach {
			case (v: Int, i)    => stmt.setInt(i + 1, v)
			case (v: String, i) => stmt.setString(i + 1, v)
			case _              =>
		}
		val items = DBHelper.select(stmt, classOf[CartItem]).map(_.entity)
		stmt.close()
		items.headOption
	}

	def updateItemQuantity(cartItemId: Int, quantity: Int)(implicit conn: Connection): Unit = {
		if (quantity <= 0) {
			removeItem(cartItemId)
		} else {
			val sql = "UPDATE CartItem SET quantity = ? WHERE id = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, quantity)
			stmt.setInt(2, cartItemId)
			stmt.executeUpdate()
		}
	}

	def removeItem(cartItemId: Int)(implicit conn: Connection): Unit = {
		val sql = "DELETE FROM CartItem WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, cartItemId)
		stmt.executeUpdate()
	}

	def getItems(cartId: Int)(implicit conn: Connection): Seq[CartItem] = {
		val sql = "SELECT * FROM CartItem JOIN ShopProduct sp ON sp.id = CartItem.product_id JOIN Cart c ON c.id = CartItem.cart_id WHERE cart_id = ? ORDER BY id"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, cartId)
		val items = DBHelper.selectDeep(stmt)(conn, classOf[CartItem])
		stmt.close()
		items
	}

	def clearCart(cartId: Int)(implicit conn: Connection): Unit = {
		val sql = "DELETE FROM CartItem WHERE cart_id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, cartId)
		stmt.executeUpdate()
	}

	def getTotalPrice(items: Seq[CartItem]): BigDecimal = {
		items.foldLeft(BigDecimal.ZERO) { (acc, item) =>
			val effectivePrice = if (item.product != null && item.product.discountPrice != null) {
				item.product.discountPrice
			} else if (item.product != null) {
				item.product.price
			} else {
				BigDecimal.ZERO
			}
			acc.add(effectivePrice.multiply(BigDecimal.valueOf(item.quantity)))
		}
	}

	def saveShippingAndPayment(cartId: Int, shippingMethodId: Int, paymentMethodId: Int)(implicit conn: Connection): Unit = {
		val sql = "UPDATE Cart SET selectedShippingMethod_id = ?, selectedPaymentMethod_id = ?, updatedAt = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, shippingMethodId)
		stmt.setInt(2, paymentMethodId)
		stmt.setObject(3, LocalDateTime.now())
		stmt.setInt(4, cartId)
		stmt.executeUpdate()
	}

	def deleteCart(cartId: Int)(implicit conn: Connection): Unit = {
		clearCart(cartId)
		val sql = "DELETE FROM Cart WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, cartId)
		stmt.executeUpdate()
	}
}
