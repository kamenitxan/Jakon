package utils.entity

import java.sql.Connection

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.SqlGen
import cz.kamenitxan.jakon.webui.entity.JakonField

/**
 * Created by TPa on 02.03.2020.
 */
class TestExtUser extends JakonUser {
	childClass = implicitly[sourcecode.FullName].value

	@JakonField
	var someStuff: String = _

	override def createObject(jid: Int, conn: Connection): Int = {
		super.createObject(jid, conn)
		val stmt = SqlGen.insertStmt(this, conn, jid)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		super.updateObject(jid, conn)
		val stmt = SqlGen.updateStmt(this, conn, jid)
		stmt.executeUpdate()
	}
}
