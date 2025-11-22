package cz.kamenitxan.jakon.core.database

trait Crud {
	def create(): Int

	def update(): Unit

	def delete(): Unit
}
