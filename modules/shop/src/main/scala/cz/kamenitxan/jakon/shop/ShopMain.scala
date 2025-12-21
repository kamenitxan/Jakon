package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.logging.Logger

/**
 * Created by Kamenitxan on 21.12.2025
 */
object ShopMain {

	def main(args: Array[String]): Unit = {
		Logger.info("Starting Jakon")
		val app = new JakonApp()
		app.run(args)
	}

	class JakonApp() extends ShopInit {

	}
}
