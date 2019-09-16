package cz.kamenitxan.jakon.core.model

import java.lang.reflect.Field
import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.Utils

trait Ordered {
	this: JakonObject => {}

	var visibleOrder: Int
	var objectOrder: Double

	def getObjectOrder: Double

	def setObjectOrder(order: Double)

	def fetchVisibleOrder(implicit conn: Connection): JakonObject = {
		Ordered.fetchVisibleOrder(this :: Nil, this.getClass).head
	}

	def updateNewObjectOrder(implicit conn: Connection): JakonObject with Ordered = {
		val objectClass = this.getClass
		val fieldRef: Field = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.equals("objectOrder")).get
		fieldRef.setAccessible(true)

		val stmt = conn.createStatement()
		val rs = stmt.executeQuery("SELECT objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder DESC LIMIT 1")

		if (rs.next()) {
			val lastOrder = rs.getInt(1)
			fieldRef.set(this, lastOrder + 10)
		} else {
			fieldRef.set(this, 10)
		}
		this
	}

	def updateOrder(formOrder: Int)(implicit conn: Connection): JakonObject with Ordered = {
		val objectClass = this.getClass
		val obj = this
		val stmt = conn.prepareStatement("SELECT id, objectOrder FROM " + objectClass.getSimpleName + " WHERE id = ?")
		stmt.setInt(1, obj.id)
		val persistedObj: JakonObject with Ordered = DBHelper.selectSingle(stmt, classOf[BasicJakonObject]).entity.asInstanceOf[JakonObject with Ordered]
		fetchVisibleOrder
		if (formOrder == persistedObj.visibleOrder) {

			return obj
		}
		val stmt2 = conn.createStatement()
		val queryResult = DBHelper.select(stmt2, "SELECT id, objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC", classOf[BasicJakonObject])
		val allObjects = queryResult.map(qr => {
			qr.entity.asInstanceOf[JakonObject with Ordered]
		}).filter(o => o.id != obj.id).toVector


		val requiredOrder = if (formOrder < 0) 0 else if (formOrder > allObjects.size) allObjects.size else formOrder - 1
		val before = if (allObjects.lift(requiredOrder - 1).isDefined && allObjects.lift(requiredOrder - 1).get.id != obj.id) {
			allObjects.lift(requiredOrder - 1)
		} else {
			Option.empty
		}
		val after = if (allObjects.lift(requiredOrder).isDefined && allObjects.lift(requiredOrder).get.id != obj.id) {
			allObjects.lift(requiredOrder)
		} else {
			Option.empty
		}

		val resultPos = if (before.isDefined && after.isDefined) {
			(after.get.objectOrder + before.get.objectOrder) / 2.0
		} else if (before.isDefined) {
			before.get.objectOrder + 10
		} else if (after.isDefined) {
			after.get.objectOrder / 2
		} else {
			10
		}
		obj.objectOrder = resultPos
		// TODO: po zmene pozice se ma do DB rovnou ulozit nova, aby se nemusel aktualizovat cely objekt
		obj
	}
}

object Ordered {

	def fetchVisibleOrder(objects: List[JakonObject], objectClass: Class[_])(implicit conn: Connection): List[JakonObject] = {
		val stmt = conn.createStatement()
		val result = DBHelper.select(stmt, "SELECT id FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC", classOf[BasicJakonObject])
		var i = 0
		val allObjects = result.map(qr => {
			i += 1
			(qr.entity.id, i)
		}).toMap
		objects.foreach(o => o.asInstanceOf[JakonObject with Ordered].visibleOrder = allObjects(o.id))
		objects
	}
}
