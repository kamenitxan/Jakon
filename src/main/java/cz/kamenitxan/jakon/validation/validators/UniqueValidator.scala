package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class UniqueValidator extends Validator {
	private val unique = "NOT_UNIQUE"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		val ann = a.asInstanceOf[Unique]
		if (value == null) {
			return Option.empty
		}

		val fieldName = field.getName
		val className = field.getDeclaringClass.getSimpleName

		DBHelper.withDbConnection(implicit conn => {
			val sql = s"""SELECT count(*) FROM $className WHERE $fieldName = \"$value\""""
			val res = DBHelper.count(sql)
			if (res > 0) {
				ValidationResult(unique)
			} else {
				Option.empty
			}
		})

	}
}
