package cz.kamenitxan.jakon.utils

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object Utils {
	def toJavaColection(list: List[AnyRef]) = {
		asJavaCollection(list)
	}

	implicit class StringImprovements(s: String) {
		def toOptInt = Try(Integer.parseInt(s)).toOption
	}
}
