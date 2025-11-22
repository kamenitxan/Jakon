package jakontest.test

import cz.kamenitxan.jakon.core.template.function.IFuncion

import java.util


class HelloFun extends IFuncion {
	override def execute(params: util.Map[String, String]): String = "helloWorld"
}
