package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.webui.functions.LinkFun
import com.mitchellbosecke.pebble.extension.{AbstractExtension, Filter, Function}

import java.util

/**
  * Created by tomaspavel on 6.10.16.
  */
class PebbleExtension extends AbstractExtension {
	override def getFunctions: util.Map[String, Function] = {
		val extensions = new util.HashMap[String, Function]()
		extensions.put("i18n", new I18nFun)
		extensions.put("link", new LinkFun)
		extensions.put("value", new ValueFun)
		extensions
	}

	override def getFilters: util.Map[String, Filter] = {
		val extensions = new util.HashMap[String, Filter]()
		extensions.put("date", new JakonDateFilter)
		extensions.put("md", new MarkdownFilter)
		extensions.put("asJava", new AsJavaFilter)
		extensions
	}
}