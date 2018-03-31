package cz.kamenitxan.jakon.core.model

import javax.json.Json
import javax.persistence._
import java.io.StringWriter

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField

import scala.beans.BeanProperty

/**
  * Created by TPa on 22.04.16.
  */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class JakonObject(@BeanProperty
                           @Column
                           @JakonField var childClass: String
                          ) extends Serializable {
	@BeanProperty
	@Id
	@GeneratedValue
	@JakonField(disabled = true, required = false, listOrder = -99)
	val id: Int = 0
	@BeanProperty
	@Column
	@JakonField var url: String = ""
	@BeanProperty
	@Column
	@JakonField var sectionName: String = ""
	@BeanProperty
	@Column
	@JakonField(listOrder = -95)
	var published: Boolean = true

	val objectSettings: ObjectSettings

	def getObjectSettings: ObjectSettings = objectSettings

	def create(): Unit = {
		val session = DBHelper.getSession
		session.beginTransaction()
		session.save(this)
		session.getTransaction.commit()
		session.close()
	}

	def update(): Unit = {
		val session = DBHelper.getSession
		session.beginTransaction()
		session.update(this)
		session.getTransaction.commit()
		session.close()
	}

	def delete(): Unit = {
		val session = DBHelper.getSession
		session.beginTransaction()
		session.delete(this)
		session.getTransaction.commit()
		session.close()
	}

	def toJson = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		writer.toString
	}
}