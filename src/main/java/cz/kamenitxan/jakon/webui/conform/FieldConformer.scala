package cz.kamenitxan.jakon.webui.conform

import java.util.Date
import java.text.SimpleDateFormat

object FieldConformer {


	implicit class StringConformer(val s: String) {
		val S = classOf[String]
		val B = classOf[Boolean]
		val D = classOf[java.lang.Double]
		val DATE = classOf[Date]

		def conform(c: Class[_]): Any = {
			if (s == null || s.isEmpty) {
				return null
			}
			c match {
				case B => s toBoolean
				case D => s toDouble
				case DATE => {
					val sdf = new SimpleDateFormat("MM/dd/yyyy")
					sdf.parse(s)
				}
				case _ => s
			}
		}

	}

}
