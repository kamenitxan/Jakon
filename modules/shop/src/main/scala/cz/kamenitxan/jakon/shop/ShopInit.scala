package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.Post
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.task.{CartCleanupTask, PaymentStatusSyncTask}

import java.util.concurrent.TimeUnit

object ShopInit {

	/**
	 * Class of shop products, read by shop pages and services. Set it before the application starts (before
	 * daoSetup) to use a direct subclass of [[ShopProduct]] with application specific fields. The subclass is
	 * registered as DAO. Variants, cart and orders still reference [[ShopProduct]] by id, so the subclass is stored
	 * in its own table joined by id and its createObject and updateObject must call super.
	 */
	var productClass: Class[? <: ShopProduct] = classOf[ShopProduct]

	/**
	 * Class of shop categories, works the same way as [[productClass]] - a direct subclass of [[ShopCategory]]
	 * stored in its own table joined by id. Parent category, product category and other relations still reference
	 * [[ShopCategory]], [[cz.kamenitxan.jakon.shop.service.ShopProductService]] replaces the category of loaded
	 * products with an instance of this class.
	 */
	var categoryClass: Class[? <: ShopCategory] = classOf[ShopCategory]
}

class ShopInit extends JakonInit {

	override def daoSetup(): Unit = {
		super.daoSetup()
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Customer])
		DBHelper.addDao(classOf[PaymentMethod])
		DBHelper.addDao(classOf[ShippingMethod])
		DBHelper.addDao(classOf[ShopCategory])
		if (ShopInit.categoryClass != classOf[ShopCategory]) {
			DBHelper.addDao(ShopInit.categoryClass)
		}
		DBHelper.addDao(classOf[ProductVariant])
		DBHelper.addDao(classOf[ProductVariantValue])
		DBHelper.addDao(classOf[ShopProduct])
		if (ShopInit.productClass != classOf[ShopProduct]) {
			DBHelper.addDao(ShopInit.productClass)
		}
		DBHelper.addDao(classOf[Cart])
		DBHelper.addDao(classOf[CartItem])
		DBHelper.addDao(classOf[ShopOrder])
		DBHelper.addDao(classOf[ShopOrderItem])
		DBHelper.addDao(classOf[OrderPayment])
		DBHelper.addDao(classOf[OrderReturn])
		DBHelper.addDao(classOf[OrderReturnItem])
	}

	override def taskSetup(): Unit = {
		super.taskSetup()
		TaskRunner.registerTask(new PaymentStatusSyncTask(5, TimeUnit.MINUTES))
		TaskRunner.registerTask(new CartCleanupTask)
	}

	override protected def afterInit(): Unit = {
		super.afterInit()
		DBHelper.withDbConnection(implicit conn => {
			ShopTestDataSeeder.seedIfEmpty()
		})
	}
}
