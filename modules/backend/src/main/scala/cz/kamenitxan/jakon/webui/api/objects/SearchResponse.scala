package cz.kamenitxan.jakon.webui.api.objects

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.api.AbstractResponse

import scala.jdk.CollectionConverters._

/**
  * Created by TPa on 29.04.18.
  */
class SearchResponse private(val result: java.util.List[Result], success: Boolean) extends AbstractResponse(success) {

	def this(success: Boolean, result: List[JakonObject]) = {
		this(result.map(o => Result(o.id, o.toString)).asJava, success)
	}

}

case class Result(id: Integer, name: String)