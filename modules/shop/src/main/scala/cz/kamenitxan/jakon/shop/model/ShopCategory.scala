package cz.kamenitxan.jakon.shop.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

class ShopCategory extends JakonObject with Serializable {

	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-folder")
	
	@JakonField(searched = true)
	var displayOrder: Int = 0
	
	@JakonField(required = false)
	var image: String = ""
	
	@JakonField(required = false)
	var description: String = ""
	
	@JakonField(searched = true)
	@NotEmpty
	var name: String = ""
	
}
	





