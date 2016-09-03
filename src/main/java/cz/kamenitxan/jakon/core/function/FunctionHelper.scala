package cz.kamenitxan.jakon.core.function

import java.util

/**
  * Created by TPa on 25.05.16.
  */
object FunctionHelper {
	private val functions = new util.HashMap[String, IFuncion]

	register(new Link)

	def register(f: IFuncion) {
		functions.put(f.getName, f)
	}

	def getFunction(name: String): IFuncion = {
		functions.get(name)
	}

	def splitParams(params: String): util.Map[String, String] = {
		val parsed = new util.HashMap[String, String]
		val p1 = params.split(" ")
		for (s <- p1) {
			val split = s.split("=")
			if (split.length == 2) {
				parsed.put(split(0), split(1))
			}
			else {
				parsed.put(split(0), split(0))
			}
		}
		parsed
	}
}