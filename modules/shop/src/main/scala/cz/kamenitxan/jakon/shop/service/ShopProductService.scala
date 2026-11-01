package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopInit
import cz.kamenitxan.jakon.shop.entity.{ShopCategory, ShopProduct}

import java.sql.Connection

object ShopProductService {

	/**
	 * FROM clause selecting products of the given class, which is either ShopProduct or its direct subclass.
	 * Table of the subclass is joined to ShopProduct by id, JakonObject is joined as well so published and url
	 * are available.
	 */
	def fromClause(cls: Class[? <: ShopProduct] = ShopInit.productClass): String = {
		val subclassJoin = if (cls == classOf[ShopProduct]) "" else s" JOIN ${cls.getSimpleName} ON ${cls.getSimpleName}.id = ShopProduct.id"
		s"ShopProduct$subclassJoin JOIN JakonObject ON JakonObject.id = ShopProduct.id"
	}

	/** All published products including variants. */
	def getAll()(implicit conn: Connection): Seq[ShopProduct] = {
		select("JakonObject.published = true")
	}

	/** Published products for product listings - variants are represented by their main product. */
	def getListed()(implicit conn: Connection): Seq[ShopProduct] = {
		select("JakonObject.published = true AND ShopProduct.mainProduct_id IS NULL")
	}

	private def select(where: String)(implicit conn: Connection): Seq[ShopProduct] = {
		implicit val cls: Class[ShopProduct] = ShopInit.productClass.asInstanceOf[Class[ShopProduct]]
		val sql = s"SELECT * FROM ${fromClause(cls)} WHERE $where ORDER BY ShopProduct.id"
		val stmt = conn.createStatement()
		withCategoryClass(DBHelper.selectDeep(stmt, sql))
	}

	/**
	 * Category of a product is loaded by the type of the field, which is ShopCategory. When the shop uses a category
	 * subclass, the category of every product is replaced with the instance of [[ShopInit.categoryClass]].
	 */
	private def withCategoryClass(products: Seq[ShopProduct])(implicit conn: Connection): Seq[ShopProduct] = {
		if (ShopInit.categoryClass != classOf[ShopCategory] && products.exists(_.category != null)) {
			val categories = ShopCategoryService.getAll().map(c => c.id -> c).toMap
			products.foreach(p => {
				if (p.category != null) {
					categories.get(p.category.id).foreach(p.category = _)
				}
			})
		}
		products
	}
}
