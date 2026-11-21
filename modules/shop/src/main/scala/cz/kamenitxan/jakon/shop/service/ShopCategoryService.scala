package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopInit
import cz.kamenitxan.jakon.shop.entity.ShopCategory

import java.sql.Connection

/**
 * Created by Kamenitxan on 22.12.2025
 */
object ShopCategoryService {

	/**
	 * FROM clause selecting categories of the given class, which is either ShopCategory or its direct subclass.
	 * Table of the subclass is joined to ShopCategory by id.
	 */
	def fromClause(cls: Class[? <: ShopCategory] = ShopInit.categoryClass): String = {
		val subclassJoin = if (cls == classOf[ShopCategory]) "" else s" JOIN ${cls.getSimpleName} ON ${cls.getSimpleName}.id = ShopCategory.id"
		s"ShopCategory$subclassJoin"
	}

	def getAll()(implicit conn: Connection): Seq[ShopCategory] = {
		implicit val cls: Class[ShopCategory] = ShopInit.categoryClass.asInstanceOf[Class[ShopCategory]]
		//language=SQL
		val sql = s"SELECT * FROM ${fromClause(cls)} LEFT JOIN ShopCategory p ON ShopCategory.parentCategory_id = p.id JOIN JakonObject jo ON jo.id = ShopCategory.id ORDER BY p.id;"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}

}
