package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.util.Locale

import scala.collection.JavaConversions._
import scala.language.postfixOps
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

	def getFieldsUpTo(startClass: Class[_],  exclusiveParent: Class[_]): List[Field] = {
		var currentClassFields = startClass.getDeclaredFields toList
		val parentClass = startClass.getSuperclass
		if (parentClass != null && (exclusiveParent == null || !(parentClass == exclusiveParent))) {
			var parentClassFields: List[Field] = getFieldsUpTo(parentClass, exclusiveParent)
			currentClassFields = parentClassFields ::: currentClassFields
		}
		currentClassFields
	}

	def stringToLocale(s: String): Locale = {
		if (s == null) return null
		val split = s.split("_")
		new Locale(split(0), split(1))
	}
}
