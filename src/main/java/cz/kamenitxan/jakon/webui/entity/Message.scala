package cz.kamenitxan.jakon.webui.entity

class Message(val _severity: MessageSeverity, val text: String, val params: Seq[String] = Seq[String](), val bundle: String = "messages") {
	def severity(): String = _severity.value

	override def toString = s"Message($text, ${_severity})"
}
