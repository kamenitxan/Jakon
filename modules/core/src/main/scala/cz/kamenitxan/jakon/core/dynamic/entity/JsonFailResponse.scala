package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
class JsonFailResponse[T](val d: T) extends AbstractJsonResponse[T](status = ResponseStatus.fail, data = d) {

}
