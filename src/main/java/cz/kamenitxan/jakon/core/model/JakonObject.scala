package cz.kamenitxan.jakon.core.model

import javax.json.Json
import javax.json.stream.JsonGenerator
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import java.io.StringWriter

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

	def toJson: String = {
		val writer: StringWriter = new StringWriter
		val generator: JsonGenerator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		return writer.toString
	}
}