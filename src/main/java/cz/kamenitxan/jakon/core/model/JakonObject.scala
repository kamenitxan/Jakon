package cz.kamenitxan.jakon.core.model

import javax.json.Json
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import java.io.StringWriter

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings

import scala.beans.BeanProperty

/**
  * Created by TPa on 22.04.16.
  */
abstract class JakonObject {
	@BeanProperty
	@Id
	@GeneratedValue val id: Int = 0
	@BeanProperty
	@Column var url: String = ""
	@BeanProperty
	@Column var sectionName: String = ""
	@BeanProperty
	@Column var published: Boolean = true

	val objectSettings: ObjectSettings

	def getObjectSettings = objectSettings

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