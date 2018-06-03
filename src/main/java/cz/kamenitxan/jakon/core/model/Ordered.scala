package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.webui.entity.JakonField

trait Ordered {
	var visibleOrder: Int
	var objectOrder: Double

	def getObjectOrder: Double

	def setObjectOrder(order: Double)
}
