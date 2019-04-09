package example

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.macros.SqlGen
import org.slf4j.LoggerFactory

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Main {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]) {

		logger.info("Starting Jakon")
		val app = new JakonApp()
		app.run(args)
		SqlGen.insert(classOf[JakonUser], DBHelper.getConnection)
	}
}