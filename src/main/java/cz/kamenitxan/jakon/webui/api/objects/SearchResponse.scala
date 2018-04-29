package cz.kamenitxan.jakon.webui.api.objects

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.api.AbstractResponse

/**
  * Created by TPa on 29.04.18.
  */
class SearchResponse(success: Boolean,
                     result: List[JakonObject]) extends AbstractResponse(success) {

}
