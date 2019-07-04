package cz.kamenitxan.jakon.core.model.Dao

trait Crud {
	def create(): Int

	def update(): Unit

	def delete(): Unit
}
