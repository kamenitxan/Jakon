package cz.kamenitxan.jakon.utils

import scala.collection.JavaConversions._

/**
  * Created by TPa on 08.09.16.
  */
object Utils {
	def toJavaColection(list: List[AnyRef]) = {
		asJavaCollection(list)
	}
}
