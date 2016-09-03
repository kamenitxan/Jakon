package cz.kamenitxan.jakon.core.function

import java.util

/**
  * Funkce jsou zapisovane v tvaru {jmenoFunkce nazevParametru=hodnotaparametru...}
  * Created by TPa on 25.05.16.
  */
trait IFuncion {
	def getName: String = {
		this.getClass.getSimpleName
	}

	def execute(params: util.Map[String, String]): String
}