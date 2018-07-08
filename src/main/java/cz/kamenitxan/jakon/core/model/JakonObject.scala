package cz.kamenitxan.jakon.core.model

import javax.json.Json
import javax.persistence._
import java.io.StringWriter

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import org.hibernate.Session

import scala.beans.BeanProperty
import scala.language.postfixOps

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
	@JakonField(disabled = true, required = false, listOrder = -99, searched = true)
	var id: Int = 0
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

	def setUrl(url: String): Unit = this.url = url

	def getUrl: String = url

	private def execute(fun: Session => Unit): Unit = {
		val session = DBHelper.getSession
		if (!session.getTransaction.isActive) {
			session.beginTransaction()
		}
		fun.apply(session)
		afterAll()
		session.getTransaction.commit()
		session.close()
	}

	private def executeWithId(fun: Session => Int): Int = {
		val session = DBHelper.getSession
		if (!session.getTransaction.isActive) {
			session.beginTransaction()
		}
		val id = fun.apply(session)
		afterAll()
		session.getTransaction.commit()
		session.close()
		id
	}

	def create(): Int = {
		executeWithId( session => {
			val id = session.save(this)
			afterCreate()
			id.asInstanceOf[Int]
		})
	}

	def afterCreate(): Unit = {}

	def update(): Unit = {
		execute(session => {
			session.update(this)
			afterUpdate()
		})
	}

	def afterUpdate(): Unit = {}

	def delete(): Unit = {
		execute(session => {
			session.delete(this)
			afterDelete()
		})
	}

	def afterDelete(): Unit = {}

	def afterAll(): Unit = {}


	override def toString: String = {
		childClass + "(id: " + id + ")"
	}

	def toJson: String = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		writer.toString
	}
}