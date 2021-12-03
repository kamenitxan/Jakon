package cz.kamenitxan.jakon.core.database

import java.lang.reflect.Field

import cz.kamenitxan.jakon.core.model.BaseEntity

class QueryResult[T <: BaseEntity](val foreignIds: Map[String, ForeignKeyInfo]) {
	// TODO: change to Option[T]
	var entity: T = _
	var i18nField: Option[Field] = Option.empty

	def this(entity: T, foreignIds: Map[String, ForeignKeyInfo], i18nField: Option[Field]) = {
		this(foreignIds)
		this.entity = entity
		this.i18nField = i18nField
	}
}
