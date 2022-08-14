package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
abstract class AbstractJsonResponse[T](val status: ResponseStatus, val data: T) {

}
