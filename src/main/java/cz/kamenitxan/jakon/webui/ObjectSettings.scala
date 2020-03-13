package cz.kamenitxan.jakon.webui

/**
  * Created by TPa on 08.09.16.
  */
class ObjectSettings(
                      /** font awesome icon show in administration */
                      val icon: String = "fa-file-o",

                      /** if true field from parent objects are not shown in administration list view  */
                      val noParentFieldInList: Boolean = false,
                    ) extends Serializable

