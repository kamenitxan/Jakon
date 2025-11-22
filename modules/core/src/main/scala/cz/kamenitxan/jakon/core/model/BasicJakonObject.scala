package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.Connection

/**
  * Created by TPa on 2018-12-25.
  */
class BasicJakonObject extends JakonObject with Ordered {

	override val objectSettings: ObjectSettings = new ObjectSettings()

	override def createObject(jid: Int, conn: Connection): Int = {
		jid
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// BasicJakonObject does nothing
	}

	var visibleOrder: Int = _
	var objectOrder: Double = _

}
