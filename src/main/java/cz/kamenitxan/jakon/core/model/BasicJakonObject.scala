package cz.kamenitxan.jakon.core.model

import java.sql.Connection

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 2018-12-25.
  */
class BasicJakonObject extends JakonObject(classOf[BasicJakonObject].getName) {

	override val objectSettings: ObjectSettings = new ObjectSettings()

	override def createObject(jid: Int, conn: Connection): Int = ???

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}
