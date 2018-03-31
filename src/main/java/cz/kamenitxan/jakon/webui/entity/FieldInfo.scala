package cz.kamenitxan.jakon.webui.entity

import java.lang.reflect.Field

class FieldInfo(val required: Boolean,
                val disabled: Boolean,
                val htmlType: String,
                val htmlClass: String,
                val htmlMaxLength: Int,
                val value: String,
                val name: String,
                val objectName: String,
                val an: JakonField) {

	def this(an: JakonField, htmlType: HtmlType, f: Field, value: String) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName,
			if (an.inputTemplate().isEmpty) {
				f.getType.getSimpleName
			} else {
				an.inputTemplate()
			}, an)
	}

	def this(an: JakonField, field: Field) = {
		this(an.required(), an.disabled(), null, an.htmlClass(), an.htmlMaxLength(), null, field.getName, null, an)
	}

	override def toString = s"FieldInfo($required, $disabled, $htmlType, $htmlClass, $value, $name)"
}

