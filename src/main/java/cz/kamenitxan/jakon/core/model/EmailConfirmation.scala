package cz.kamenitxan.jakon.core.model

import java.sql.Connection

import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.{Entity, Transient}

@Entity
class EmailConfirmation(u: Unit = ()) extends JakonObject(childClass = classOf[EmailConfirmation].getName) {


	def this() = this(u = ())

	@Transient
	override val objectSettings: ObjectSettings = ???

	override def createObject(jid: Int, conn: Connection): Int = ???

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}
