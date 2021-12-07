package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.{Gson, GsonBuilder}

/**
 * Created by TPa on 13.04.2020.
 */
abstract class AbstractJsonPagelet {
	val gson: Gson = new GsonBuilder()
	  .setPrettyPrinting()
	  .create()
}
