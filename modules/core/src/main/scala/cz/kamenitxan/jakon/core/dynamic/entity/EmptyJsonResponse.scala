package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
class EmptyJsonResponse() extends AbstractJsonResponse[String](status = ResponseStatus.success, data = null) {

}
