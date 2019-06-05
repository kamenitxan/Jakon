package cz.kamenitxan.jakon.core.customPages

import java.sql.Connection

import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.core.template.TemplateUtils

/**
  * Created by TPa on 26.04.16.
  */
abstract class AbstractCustomPage extends JakonObject(childClass = classOf[AbstractCustomPage].getName) with IControler {
	protected val engine = TemplateUtils.getEngine

	override def createObject(jid: Int, conn: Connection): Int = ???

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}