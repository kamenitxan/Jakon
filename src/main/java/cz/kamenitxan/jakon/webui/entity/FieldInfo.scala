package cz.kamenitxan.jakon.webui.entity

import java.lang.reflect.Field

class FieldInfo(val required: Boolean,
                val disabled: Boolean,
                val htmlType: String,
                val htmlClass: String,
                val htmlMaxLength: Int,
                val value: Any,
                val name: String,
                val objectName: String,
                val an: JakonField,
                val template: String
               ) {

	def this(an: JakonField, htmlType: HtmlType, f: Field, value: String) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName, f.getType.getSimpleName, an,
			if (an.inputTemplate().isEmpty) {
				f.getType.getSimpleName
			} else {
				an.inputTemplate()
			})
	}
	def this(an: JakonField, htmlType: HtmlType, f: Field, value: Any, template: String) = {
		this(an.required(), an.disabled(), htmlType.typeName, an.htmlClass(), an.htmlMaxLength(), value, f.getName, f.getType.getSimpleName, an, template)
	}

	def this(an: JakonField, field: Field) = {
		this(an.required(), an.disabled(), null, an.htmlClass(), an.htmlMaxLength(), null, field.getName, null, an, null)
	}

	override def toString = s"FieldInfo($required, $disabled, $htmlType, $htmlClass, $value, $name)"
}

