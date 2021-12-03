package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by TPa on 13.04.2020.
 */
class JsonFailResponse(val d: Any) extends AbstractJsonResponse(status = ResponseStatus.fail, data = d) {

}
