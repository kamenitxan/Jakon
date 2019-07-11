package cz.kamenitxan.jakon.core.task

import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.fulltext.Lucene
import cz.kamenitxan.jakon.webui.conform.FieldConformer

import scala.language.postfixOps

class FulltextTask() extends AbstractTask(classOf[FulltextTask].getSimpleName, 0, TimeUnit.MINUTES) {

	override def start(): Unit = {
		val classes = DBHelper.objects
		Lucene.dropIndex()
		classes.par.foreach(jo => {
			val infos = FieldConformer.getEmptyFieldInfos(jo.getFields toList)
			val indexed = infos.exists(fi => fi.an.searched())
			if (indexed) {
				// TODO
			}
		})
	}
}
