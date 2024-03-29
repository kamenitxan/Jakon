package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.Transient
import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Category extends JakonObject with Ordered {
	@JakonField(searched = true)
	var name: String = ""
	@JakonField
	var showComments: Boolean = false
	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	var objectOrder: Double = _
	@Transient
	@JakonField(listOrder = -96)
	var visibleOrder: Int = _

	override val objectSettings: ObjectSettings = null

	override def toString: String = "Category{" + "id='" + id + '\'' + ", name='" + name + '\'' + "} "

}