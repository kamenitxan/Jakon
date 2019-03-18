package cz.kamenitxan.jakon.core.model.converters

import javax.persistence.AttributeConverter

/**
  * Created by TPa on 2019-03-18.
  */
abstract class AbstractMapConverter[K, V] extends AttributeConverter[Map[K, V], String] {

}
