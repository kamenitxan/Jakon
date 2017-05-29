package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import org.slf4j.LoggerFactory

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Main {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]) {
		val arguments = args.toList.map(a => {
			val split = a.split("=")
			split.length match {
				case 1 => split(0) -> None
				case 2 => split(0) -> split(1)
			}
		})

		Director.start()
	}
}