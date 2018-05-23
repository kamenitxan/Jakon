package cz.kamenitxan.jakon.webui.functions;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;
import cz.kamenitxan.jakon.webui.functions.GetAttributeFun;
import cz.kamenitxan.jakon.webui.functions.GetAttributeTypeFun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomaspavel on 6.10.16.
 */
public class PebbleExtension extends AbstractExtension {

	@Override
	public Map<String, Function> getFunctions() {
		return new HashMap<String, Function>() {{
			put("getAttr", new GetAttributeFun());
			put("getAttrType", new GetAttributeTypeFun());
			put("link", new LinkFun());
			put("i18n", new i18nFun());
		}};
	}
}
