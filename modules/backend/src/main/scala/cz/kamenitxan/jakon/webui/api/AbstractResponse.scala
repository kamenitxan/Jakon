package cz.kamenitxan.jakon.webui.api

/**
  * Created by TPa on 29.04.18.
  */
@Deprecated
abstract class AbstractResponse(val success: Boolean) {
	var error: String = _
}
