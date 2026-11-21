package cz.kamenitxan.jakon.shop.task

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.task.AbstractTask
import cz.kamenitxan.jakon.logging.Logger

import java.util.concurrent.TimeUnit

/**
 * Periodically deletes carts that have been inactive for more than one month.
 *
 * Removes both the cart items and the cart itself for all [[cz.kamenitxan.jakon.shop.entity.Cart]]
 * records whose {@code updatedAt} timestamp is older than 30 days.
 * Runs once per day.
 */
class CartCleanupTask extends AbstractTask(1, TimeUnit.DAYS) {

	override def start(): Unit = {
		DBHelper.withDbConnection { implicit conn =>
			val deleteItems =
				// language=SQL
				"DELETE FROM CartItem WHERE cart_id IN (SELECT id FROM Cart WHERE updatedAt < datetime('now', '-30 days'))"
			val deleteItemsStmt = conn.prepareStatement(deleteItems)
			val deletedItems = deleteItemsStmt.executeUpdate()
			deleteItemsStmt.close()

			val deleteCarts =
				// language=SQL
				"DELETE FROM Cart WHERE updatedAt < datetime('now', '-30 days')"
			val deleteCartsStmt = conn.prepareStatement(deleteCarts)
			val deletedCarts = deleteCartsStmt.executeUpdate()
			deleteCartsStmt.close()

			if (deletedCarts > 0) {
				Logger.info(s"CartCleanupTask: deleted $deletedCarts cart(s) and $deletedItems item(s) older than 30 days")
			}
		}
	}
}
