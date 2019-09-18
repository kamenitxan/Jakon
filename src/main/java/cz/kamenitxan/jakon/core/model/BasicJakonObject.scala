package cz.kamenitxan.jakon.core.model

import java.sql.{Connection, Statement, Types}

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 2018-12-25.
  */
class BasicJakonObject extends JakonObject(classOf[BasicJakonObject].getName) with Ordered {

	override val objectSettings: ObjectSettings = new ObjectSettings()

	override def createObject(jid: Int, conn: Connection): Int = { jid }

	override def updateObject(jid: Int, conn: Connection): Unit = {}

	override var visibleOrder: Int = _
	override var objectOrder: Double = _

}
