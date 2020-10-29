package cz.kamenitxan.jakon.utils

import java.io.{BufferedReader, InputStreamReader}
import java.lang.reflect.Field
import java.net.URLEncoder
import java.util.Locale
import java.util.stream.Collectors

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.logging.Logger

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object Utils {

	implicit class StringImprovements(s: String) {
		def toOptInt: Option[Int] = Try(Integer.parseInt(s)).toOption

		def toBoolOrFalse: Boolean = {
			try {
				s.toBoolean
			} catch {
				case _: IllegalArgumentException => false
			}
		}

		def getOrElse(`else`: String): String = {
			if (isEmpty(s)) {
				`else`
			} else {
				s
			}
		}

		def isNullOrEmpty: Boolean = {
			if (s == null) {
				true
			} else {
				s.isEmpty
			}
		}

		def urlEncode: String = {
			if (s == null) {
				null
			} else {
				URLEncoder.encode(s, "UTF-8")
			}

		}
	}

	def getFieldsUpTo(startClass: Class[_], exclusiveParent: Class[_]): Seq[Field] = {
		var currentClassFields = getFields(startClass)
		val parentClass = startClass.getSuperclass
		if (parentClass != null && (exclusiveParent == null || (parentClass != exclusiveParent))) {
			val parentClassFields: Seq[Field] = getFieldsUpTo(parentClass, exclusiveParent)
			currentClassFields = parentClassFields ++ currentClassFields
		}
		currentClassFields
	}

	def getFields(cls: Class[_]): Seq[Field] = {
		cls.getDeclaredFields
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

	def measured[B](logFun: Long => String)(measuredFun: => B): B = {
		// TODO: https://stackoverflow.com/questions/33909930/what-is-the-best-way-to-get-the-name-of-the-caller-class-in-an-object/
		val startTime = System.currentTimeMillis()
		val result = measuredFun
		val stopTime = System.currentTimeMillis()
		val elapsedTime = stopTime - startTime
		Logger.info(logFun.apply(elapsedTime))
		result
	}

	def getResourceFromJar(name: String): Option[String] = {
		val resource = this.getClass.getResourceAsStream(name)
		if (resource != null) {
			val res = new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"))
			Option.apply(res)
		} else {
			Option.empty
		}
	}
}
