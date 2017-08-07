package cz.kamenitxan.jakon.core.model

import javax.json.Json
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import java.io.StringWriter

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField

import scala.beans.BeanProperty

/**
  * Created by TPa on 22.04.16.
  */
abstract class JakonObject {
	@BeanProperty
	@Id
	@GeneratedValue
	@JakonField val id: Int = 0
	@BeanProperty
	@Column
	@JakonField var url: String = ""
	@BeanProperty
	@Column
	@JakonField var sectionName: String = ""
	@BeanProperty
	@Column
	@JakonField var published: Boolean = true

	val objectSettings: ObjectSettings

	def getObjectSettings: ObjectSettings = objectSettings

	def create(): Unit = {
		DBHelper.getDao(this.getClass).createOrUpdate(this)
	}

	def update(): Unit = {
		DBHelper.getDao(this.getClass).createOrUpdate(this)
	}

	def toJson = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		writer.toString
	}
}