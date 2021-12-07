package test

import java.util

import cz.kamenitxan.jakon.core.function.IFuncion


class HelloFun extends IFuncion {
	override def execute(params: util.Map[String, String]): String = "helloWorld"
}
