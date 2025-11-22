package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
class JsonErrorResponse[T](val d: T, val code: Int, val message: String) extends AbstractJsonResponse[T](status = ResponseStatus.error, data = d) {

}
