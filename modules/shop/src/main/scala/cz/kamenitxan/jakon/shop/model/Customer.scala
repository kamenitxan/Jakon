package cz.kamenitxan.jakon.shop.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.Connection

/**
 * Zákazník e-shopu (rozšíření JakonUser)
 */
class Customer extends JakonUser with Serializable {

	@JakonField(searched = true, required = false)
	var phone: String = ""

	@JakonField(searched = true, required = false)
	var company: String = ""

	@JakonField(searched = true, required = false)
	var ico: String = ""

	@JakonField(searched = true, required = false)
	var dic: String = ""

	@JakonField(required = false)
	var street: String = ""

	@JakonField(required = false)
	var city: String = ""

	@JakonField(required = false)
	var zip: String = ""

	@JakonField(required = false)
	var country: String = ""

	@JakonField(required = false)
	var deliveryStreet: String = ""

	@JakonField(required = false)
	var deliveryCity: String = ""

	@JakonField(required = false)
	var deliveryZip: String = ""

	@JakonField(required = false)
	var deliveryCountry: String = ""

	override val objectSettings: ObjectSettings = Customer.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		// Nejdřív vytvoříme základního uživatele
		val userId = super.createObject(jid, conn)

		// Pak přidáme specifická data zákazníka
		// language=SQL
		val sql = "INSERT INTO Customer (id, phone, company, ico, dic, street, city, zip, country, deliveryStreet, deliveryCity, deliveryZip, deliveryCountry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, userId)
		stmt.setString(2, phone)
		stmt.setString(3, company)
		stmt.setString(4, ico)
		stmt.setString(5, dic)
		stmt.setString(6, street)
		stmt.setString(7, city)
		stmt.setString(8, zip)
		stmt.setString(9, country)
		stmt.setString(10, deliveryStreet)
		stmt.setString(11, deliveryCity)
		stmt.setString(12, deliveryZip)
		stmt.setString(13, deliveryCountry)
		stmt.executeUpdate()

		userId
	}

	override def toString: String = {
		s"Customer(id: $id, $email, $company)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		super.updateObject(jid, conn)

		// language=SQL
		val sql = "UPDATE Customer SET phone = ?, company = ?, ico = ?, dic = ?, street = ?, city = ?, zip = ?, country = ?, deliveryStreet = ?, deliveryCity = ?, deliveryZip = ?, deliveryCountry = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, phone)
		stmt.setString(2, company)
		stmt.setString(3, ico)
		stmt.setString(4, dic)
		stmt.setString(5, street)
		stmt.setString(6, city)
		stmt.setString(7, zip)
		stmt.setString(8, country)
		stmt.setString(9, deliveryStreet)
		stmt.setString(10, deliveryCity)
		stmt.setString(11, deliveryZip)
		stmt.setString(12, deliveryCountry)
		stmt.setInt(13, jid)
		stmt.executeUpdate()
	}
}

object Customer {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-user-circle")
}

