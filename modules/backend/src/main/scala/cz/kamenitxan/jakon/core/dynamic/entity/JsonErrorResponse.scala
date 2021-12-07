package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
class JsonErrorResponse(val d: Any, val code: Int, val message: String) extends AbstractJsonResponse(status = ResponseStatus.error, data = d) {

}
