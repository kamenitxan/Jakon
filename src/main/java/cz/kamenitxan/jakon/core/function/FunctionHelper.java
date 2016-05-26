package cz.kamenitxan.jakon.core.function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by TPa on 25.05.16.
 */
public class FunctionHelper {
	private static Map<String, IFuncion> functions = new HashMap<>();

	static {
		register(new Link());
	}

	public static void register(IFuncion f) {
		functions.put(f.getName(), f);
	}

	public static IFuncion getFunction(String name) {
		return functions.get(name);
	}

	public static Map<String, String> splitParams(String params) {
		Map<String, String> parsed = new HashMap<>();
		String[] p1 = params.split(" ");
		for (String s : p1) {
			String[] split = s.split("=");
			if (split.length == 2) {
				parsed.put(split[0], split[1]);
			} else {
				parsed.put(split[0], split[0]);
			}
		}
		return parsed;
	}


}
