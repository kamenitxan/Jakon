package jakontest.core.pagelet.entity

import cz.kamenitxan.jakon.core.dynamic.entity.ResponseStatus

/**
  * Created by TPa on 13.04.2020.
  */
class GetResponse {
	var status: ResponseStatus = scala.compiletime.uninitialized
	var data: String = scala.compiletime.uninitialized
}
