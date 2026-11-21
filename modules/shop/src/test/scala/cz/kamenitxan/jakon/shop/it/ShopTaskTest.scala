package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.payments.{PaymentStatus, TestPaymentGateway}
import cz.kamenitxan.jakon.shop.service.CartService
import cz.kamenitxan.jakon.shop.task.{CartCleanupTask, PaymentStatusSyncTask}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.TimeUnit

@DoNotDiscover
class ShopTaskTest extends AnyFunSuite with ShopDbSupport {

	test("CartCleanupTask deletes only carts inactive for more than 30 days") {
		val (oldCart, freshCart) = withConn(implicit conn => {
			val old = CartService.getOrCreateCart(unique("old"))
			CartService.addItem(old.id, createProduct().id, 1)
			val fresh = CartService.getOrCreateCart(unique("fresh"))
			val stmt = conn.prepareStatement("UPDATE Cart SET updatedAt = datetime('now', '-40 days') WHERE id = ?")
			stmt.setInt(1, old.id)
			stmt.executeUpdate()
			stmt.close()
			(old, fresh)
		})

		new CartCleanupTask().start()

		assert(queryInt("SELECT COUNT(*) FROM Cart WHERE id = ?", oldCart.id) == 0)
		assert(queryInt("SELECT COUNT(*) FROM CartItem WHERE cart_id = ?", oldCart.id) == 0)
		assert(queryInt("SELECT COUNT(*) FROM Cart WHERE id = ?", freshCart.id) == 1)

		// nothing left to delete
		new CartCleanupTask().start()
	}

	test("PaymentStatusSyncTask refreshes pending payments and survives gateway failures") {
		val order = createOrder()
		val completed = createOrderPayment(order, TestPaymentGateway.Code, unique("completed"))
		val broken = createOrderPayment(order, TestPaymentGateway.Code, unique("broken"))
		val unchanged = createOrderPayment(order, TestPaymentGateway.Code, unique("pending"))

		new PaymentStatusSyncTask(5, TimeUnit.MINUTES).start()

		assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", completed.id) == PaymentStatus.Completed)
		assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", broken.id) == PaymentStatus.Pending)
		assert(queryString("SELECT status FROM OrderPayment WHERE id = ?", unchanged.id) == PaymentStatus.Pending)
	}
}
