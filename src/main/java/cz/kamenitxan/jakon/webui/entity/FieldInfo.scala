package cz.kamenitxan.jakon.webui.entity

class FieldInfo(
               val required: Boolean,
               val editable: Boolean,
               val htmlType: String,
               val htmlClass: String,
               val value: String,
               val name: String,
               val objectName: String
               ) {

    override def toString = s"FieldInfo($required, $editable, $htmlType, $htmlClass, $value, $name)"
}

