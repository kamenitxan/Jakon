package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.model.JakonObject

/**
  * Created by TPa on 26.04.16.
  */
abstract class CustomPage extends JakonObject(childClass = classOf[CustomPage].getName) with IControler {

}