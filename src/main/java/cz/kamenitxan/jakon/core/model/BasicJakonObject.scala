package cz.kamenitxan.jakon.core.model

import java.sql.{Connection, Statement, Types}

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 2018-12-25.
  */
class BasicJakonObject extends JakonObject(classOf[BasicJakonObject].getName) with Ordered {

	override val objectSettings: ObjectSettings = new ObjectSettings()

	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO BasicJakonObject (id) VALUES (?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {}

	override var visibleOrder: Int = _
	override var objectOrder: Double = _

	override def getObjectOrder: Double = objectOrder

	override def setObjectOrder(order: Double): Unit = this.objectOrder = order
}
