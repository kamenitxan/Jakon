package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}

/**
 * Shop-wide configuration read from {@code jakon_config.properties}.
 *
 * Return address keys (used on the return confirmation page):
 * - {@code shop.return.name}    — company / recipient name
 * - {@code shop.return.street}  — street and number
 * - {@code shop.return.city}    — city
 * - {@code shop.return.zip}     — ZIP code
 * - {@code shop.return.country} — country
 */
@Configuration
object ShopSettings {
	@ConfigurationValue(name = "shop.return.name", required = false, defaultValue = "")
	var returnName: String = _

	@ConfigurationValue(name = "shop.return.street", required = false, defaultValue = "")
	var returnStreet: String = _

	@ConfigurationValue(name = "shop.return.city", required = false, defaultValue = "")
	var returnCity: String = _

	@ConfigurationValue(name = "shop.return.zip", required = false, defaultValue = "")
	var returnZip: String = _

	@ConfigurationValue(name = "shop.return.country", required = false, defaultValue = "")
	var returnCountry: String = _
}
