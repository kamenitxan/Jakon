package cz.kamenitxan.jakon.core.model

trait Ordered {
	var visibleOrder: Int
	var objectOrder: Double

	def getObjectOrder: Double

	def setObjectOrder(order: Double)
}
