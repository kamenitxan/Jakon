package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.webui.ObjectSettings

/**
  * Created by TPa on 2018-12-25.
  */
class BasicJakonObject extends JakonObject(classOf[BasicJakonObject].getName) {

	override val objectSettings: ObjectSettings = new ObjectSettings()
}
