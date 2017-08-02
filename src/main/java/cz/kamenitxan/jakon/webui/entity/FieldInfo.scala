package cz.kamenitxan.jakon.webui.entity

class FieldInfo(
               val required: Boolean,
               val editable: Boolean,
               val htmlType: String,
               val htmlClass: String,
               val value: String,
               val renderedValue: String,
               val name: String
               ) {

}

