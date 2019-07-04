package cz.kamenitxan.jakon.core.model.Dao

import cz.kamenitxan.jakon.core.model.JakonObject

class QueryResult[T <: JakonObject](val foreignIds: Map[String, ForeignKeyInfo]) {
	var entity: T = _

	def this(entity: T, foreignIds: Map[String, ForeignKeyInfo]) = {
		this(foreignIds)
		this.entity = entity
	}
}
