package cz.kamenitxan.jakon.webui.entity

class FieldInfo(
               val required: Boolean,
               val disabled: Boolean,
               val htmlType: String,
               val htmlClass: String,
               val value: String,
               val name: String,
               val objectName: String
               ) {

    override def toString = s"FieldInfo($required, $disabled, $htmlType, $htmlClass, $value, $name)"
}

