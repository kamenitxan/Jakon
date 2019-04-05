package cz.kamenitxan.jakon.core.template.pebble

import java.time.LocalDateTime
import java.util

import scala.language.postfixOps
import java.time.format.DateTimeFormatter
import com.mitchellbosecke.pebble.extension.core.DateFilter

class JakonDateFilter extends DateFilter{
	val formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss")

	override def apply(input: Any, args: util.Map[String, AnyRef]): AnyRef = {
		if (!input.isInstanceOf[LocalDateTime]) {
			return super.apply(input, args)
		}
		val date  = input.asInstanceOf[LocalDateTime]
		date.format(formatter)
	}
}
