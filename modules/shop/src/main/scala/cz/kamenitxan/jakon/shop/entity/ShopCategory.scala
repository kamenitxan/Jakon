package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.Utils.StringImprovements
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}

class ShopCategory extends JakonObject with Serializable {

	@JakonField(searched = true)
	@NotEmpty
	var name: String = ""
	@JakonField(required = false)
	var description: String = ""
	@JakonField(searched = true)
	var displayOrder: Int = 0
	@JakonField(required = false)
	var image: String = ""
	@ManyToOne
	@JakonField(required = false, searched = true)
	var parentCategory: ShopCategory = _
	
	/** not saved in a database */
	var children: Seq[ShopCategory] = Seq.empty

	override val objectSettings: ObjectSettings = ShopCategory.objectSettings

	override def createUrl: String = {
		s"/category/$id-${name.urlEncode}.html"
	}

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShopCategory (id, name, description, image, displayOrder, parentCategory_id) VALUES (?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setString(3, description)
		stmt.setString(4, image)
		stmt.setInt(5, displayOrder)
		if (parentCategory != null) {
			stmt.setInt(6, parentCategory.id)
		} else {
			stmt.setNull(6, Types.INTEGER)
		}

		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShopCategory SET name = ?, description = ?, image = ?, displayOrder = ?, parentCategory_id = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setString(2, description)
		stmt.setString(3, image)
		stmt.setInt(4, displayOrder)
		if (parentCategory != null) {
			stmt.setInt(5, parentCategory.id)
		} else {
			stmt.setNull(5, Types.INTEGER)
		}
		stmt.setInt(6, jid)
		stmt.executeUpdate()
	}

	override def toString: String = {
		s"ShopCategory(id: $id, $name)"
	}
	
}

object ShopCategory {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-folder")
}
