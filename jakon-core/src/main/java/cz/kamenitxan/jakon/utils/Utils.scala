package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.util
import java.util.Locale

import cz.kamenitxan.jakon.core.model.JakonObject

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.language.postfixOps
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object Utils {
	def toJavaCollection(list: List[AnyRef]): util.Collection[AnyRef] = {
		asJavaCollection(list)
	}

	implicit class StringImprovements(s: String) {
		def toOptInt = Try(Integer.parseInt(s)).toOption

		def getOrElse(`else`: String ): String = {
			if (isEmpty(s)) {
				`else`
			} else {
				s
			}
		}
	}

	def getFieldsUpTo(startClass: Class[_], exclusiveParent: Class[_]): List[Field] = {
		var currentClassFields = startClass.getDeclaredFields toList
		val parentClass = startClass.getSuperclass
		if (parentClass != null && (exclusiveParent == null || !(parentClass == exclusiveParent))) {
			var parentClassFields: List[Field] = getFieldsUpTo(parentClass, exclusiveParent)
			currentClassFields = parentClassFields ::: currentClassFields
		}
		currentClassFields
	}

	@tailrec
	def isJakonObject(cls: Class[_]): Boolean = {
		if (cls == classOf[JakonObject]) {
			true
		} else if (cls == classOf[Object] || cls.getSuperclass == null) {
			false
		} else {
			isJakonObject(cls.getSuperclass)
		}
	}

	@tailrec
	def getClassByFieldName(startClass: Class[_], fieldName: String): (Class[_], Field) = {
		var field: Option[Field] = null
		try {
			field = Option.apply(startClass.getDeclaredField(fieldName))
		} catch {
			case _: NoSuchFieldException => field = Option.empty
		}
		if (field.isEmpty) {
			getClassByFieldName(startClass.getSuperclass, fieldName)
		} else {
			startClass -> field.get
		}
	}

	def stringToLocale(s: String): Locale = {
		if (s == null) return null
		val split = s.split("_")
		new Locale(split(0), split(1))
	}

	def isEmpty(s: String): Boolean = {
		s == null || s.isEmpty
	}

	def nonEmpty(s: String): Boolean = {
		!isEmpty(s)
	}
}
