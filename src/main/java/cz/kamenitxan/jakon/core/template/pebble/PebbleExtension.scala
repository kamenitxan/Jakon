package cz.kamenitxan.jakon.core.template.pebble

import java.util

import com.mitchellbosecke.pebble.extension.{AbstractExtension, Function}

/**
  * Created by tomaspavel on 6.10.16.
  */
class PebbleExtension extends AbstractExtension {
	override def getFunctions: util.Map[String, Function] = {
		val extensions = new util.HashMap[String, Function]()
		extensions.put("i18n", new i18nFun)
		extensions
	}
}