package cz.kamenitxan.jakon.webui.entity

import java.lang.reflect.Field
import java.util

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.TypeReferences._

class FieldInfo(val required: Boolean,
                val disabled: Boolean,
                val htmlType: String,
                val htmlClass: String,
                val htmlMaxLength: Int,
                val value: Any,
                val name: String,
                val objectName: String,
                val an: JakonField,
                val template: String,
                val field: Field,
                val formatter: String
               ) {

	val extraData = new util.HashMap[String, Any]()

	def this(an: JakonField, htmlType: HtmlType, f: Field, value: Any) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName, f.getType.getSimpleName, an,
			if (an.inputTemplate().isEmpty) {
				f.getType.getSimpleName
			} else {
				an.inputTemplate()
			}, f, FieldInfo.calculateFormatter(f))
	}

	def this(an: JakonField, htmlType: HtmlType, f: Field, value: Any, template: String) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName, f.getType.getSimpleName, an, template, f, FieldInfo.calculateFormatter(f))
	}

	def this(an: JakonField, htmlType: HtmlType, f: Field, value: Any, template: String, typeName: String) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName, typeName, an, template, f, FieldInfo.calculateFormatter(f))
	}

	def this(an: JakonField, f: Field) = {
		this(an.required(), an.disabled(), null, an.htmlClass(), an.htmlMaxLength(), null, f.getName, f.getType.getSimpleName, an, null, f, FieldInfo.calculateFormatter(f))
	}


	override def toString = s"FieldInfo($required, $disabled, $htmlType, $htmlClass, $value, $name, $formatter)"
}

object FieldInfo {
	def calculateFormatter(field: Field): String = {
		field.getType match {
			case t if Utils.isJakonObject(t) => "linked"
			case BOOLEAN => "boolean"
			case DATETIME | DATE | DATE_o => "date"
			case SEQ | LIST_j => "list"
			case _ => "none"
		}
	}
}

