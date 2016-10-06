package cz.kamenitxan.jakon.utils.validation;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;
import cz.kamenitxan.jakon.utils.GetAttributeFun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomaspavel on 6.10.16.
 */
public class PebbleExtension extends AbstractExtension {

	public Map<String, Function> getFunctions() {
		return new HashMap<String, Function>() {{
			put("getAttr", new GetAttributeFun());
		}};
	}
}
