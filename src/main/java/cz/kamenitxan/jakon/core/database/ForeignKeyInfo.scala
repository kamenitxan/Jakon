package cz.kamenitxan.jakon.core.database

import java.lang.reflect.Field

class ForeignKeyInfo(val ids: Seq[Int], val columnName: String, val field: Field)
