package cz.kamenitxan.jakon.core.model.Dao

import cz.kamenitxan.jakon.core.model.JakonObject

class QueryResult(val entity: JakonObject, val foreignIds: Map[String, Int])
