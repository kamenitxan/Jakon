package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence._

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Category extends JakonObject with Ordered {
	@JakonField(searched = true)
	var name: String = ""
	@JakonField
	var showComments: Boolean = false
	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	override var objectOrder: Double = _
	@Transient
	@JakonField(listOrder = -96)
	override var visibleOrder: Int = _

	@Transient
	override val objectSettings: ObjectSettings = null

	override def toString: String = "Category{" + "id='" + id + '\'' + ", name='" + name + '\'' + "} "

}