package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence._

import scala.beans.BeanProperty

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
@Entity
class Category(u: Unit = ()) extends JakonObject(classOf[Category].getName) with Ordered {
	@BeanProperty
	@Column
	@JakonField(searched = true)
	var name: String = ""
	@BeanProperty
	@Column
	@JakonField
	var showComments: Boolean = false
	@Column(nullable = false)
	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	override var objectOrder: Double = _
	@Transient
	@JakonField(listOrder = -96)
	override var visibleOrder: Int = _

	def this() = this(u = ())

	@Transient
	override val objectSettings: ObjectSettings = null

	override def toString: String = "Category{" + "id='" + id + '\'' + ", name='" + name + '\'' + "} "

}