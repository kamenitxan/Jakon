package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.model.JakonObject

/**
  * Created by TPa on 26.04.16.
  */
abstract class CustomPage extends JakonObject(childClass = classOf[CustomPage].getName) {

	def render() {
		throw new UnsupportedOperationException("Render method for class " + this.getClass.getSimpleName + " is not implemented")
	}
}