package cz.kamenitxan.jakon.core.function

import java.util
import java.util.regex.{Matcher, Pattern}

/**
  * Created by TPa on 25.05.16.
  */
object FunctionHelper {
	private val functions = new util.HashMap[String, IFuncion]
	val functionPattern = Pattern.compile("(?:\\{)([a-z,A-Z]+)(.*)(?:\\})")
	val paramsPattern = Pattern.compile("(?:\\{)([a-z,A-Z]+)(.*)(?:\\})")

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

	/**
	  * Parses whole string and evaluates functions
	  * @param text input text
	  * @return input with functions replaced with their result
	  */
	def parse(text: String): String = {
		val m = functionPattern.matcher(text)
		val result = new StringBuffer
		while (m.find) {
			val funcion = m.group(1)
			val params = m.group(2)
			val fun = FunctionHelper.getFunction(funcion)
			if (fun != null) {
				m.appendReplacement(result, FunctionHelper.getFunction(funcion).execute(FunctionHelper.splitParams(params)))
			}
		}
		m.appendTail(result)
		result.toString
	}
}