package utils.entity

import java.sql.Connection

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.SqlGen

/**
 * Created by TPa on 02.03.2020.
 */
class TestExtNoFields extends JakonUser {
	childClass = implicitly[sourcecode.FullName].value

	override def createObject(jid: Int, conn: Connection): Int = {
		super.createObject(jid, conn)
		val stmt = SqlGen.insertStmt(this, conn, jid)
		executeInsert(stmt)
	}

}
