package cz.kamenitxan.jakon.core.database.converters

import com.google.gson.Gson

/**
  * Created by TPa on 2019-03-19.
  */
abstract class AbstractConverter[T] extends AttributeConverter[T, String] {
	protected val gson = new Gson()

}
