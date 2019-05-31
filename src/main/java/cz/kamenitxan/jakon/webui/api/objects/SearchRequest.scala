package cz.kamenitxan.jakon.webui.api.objects

import cz.kamenitxan.jakon.webui.api.AbstractRequest

/**
  * Created by TPa on 29.04.18.
  */
class SearchRequest(val objectName: String, val query: String) extends AbstractRequest(objectName) {

}
