package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
/**
  * Created by tomaspavel on 6.10.16.
  */
class AdminPebbleExtension extends PebbleExtension {
	override def getFunctions: util.Map[String, Function] = {
		val extensions = super.getFunctions
		extensions.put("getAttr", new GetAttributeFun)
		extensions.put("getAttrType", new GetAttributeTypeFun)
		extensions.put("i18n", new I18nFun)
		extensions.put("splitMessages", new SplitMessagesFun)
		extensions.put("objectExtensions", new ObjectExtensionFun)
		extensions.put("getAdminControllers", new GetAdminControllers)
		extensions
	}
}