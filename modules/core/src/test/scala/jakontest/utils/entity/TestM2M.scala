package jakontest.utils.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToMany
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement}

/** Target of ManyToMany relation in tests. */
class TestM2MTag extends JakonObject {
	@JakonField
	var name: String = ""

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-tag")

	override def createObject(jid: Int, conn: Connection): Int = {
		val stmt = conn.prepareStatement("INSERT INTO TestM2MTag (id, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val stmt = conn.prepareStatement("UPDATE TestM2MTag SET name = ? WHERE id = ?")
		stmt.setString(1, name)
		stmt.setInt(2, jid)
		stmt.executeUpdate()
	}
}

/** Owner of ManyToMany relation in tests. Junction table TestM2MOwnerTestM2MTag is created by DBInitializer. */
class TestM2MOwner extends JakonObject {
	@JakonField
	var name: String = ""

	@ManyToMany(genericClass = classOf[TestM2MTag])
	@JakonField(required = false)
	var tags: Seq[TestM2MTag] = Seq.empty

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-tags")

	override def createObject(jid: Int, conn: Connection): Int = {
		val stmt = conn.prepareStatement("INSERT INTO TestM2MOwner (id, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val stmt = conn.prepareStatement("UPDATE TestM2MOwner SET name = ? WHERE id = ?")
		stmt.setString(1, name)
		stmt.setInt(2, jid)
		stmt.executeUpdate()
	}
}
