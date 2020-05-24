package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.webui.entity.SortDirection

/**
  * Created by TPa on 08.09.16.
  */
class ObjectSettings(
                      /** font awesome icon show in administration */
                      val icon: String = "fa-file-o",

                      /** if true field from parent objects are not shown in administration list view  */
                      val noParentFieldInList: Boolean = false,

                      /** default sort in administration list view */
                      val sortDirection: SortDirection = SortDirection.ASC
                    ) extends Serializable

