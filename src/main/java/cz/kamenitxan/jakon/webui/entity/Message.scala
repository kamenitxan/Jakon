package cz.kamenitxan.jakon.webui.entity

import scala.collection.JavaConverters._

class Message(val _severity: MessageSeverity, val text: String, val _params: List[String] = List[String]()) {
	def severity(): String = _severity.value

	def params(): java.util.List[String] = _params.asJava


	override def toString = s"Message($text, ${_severity})"
}
