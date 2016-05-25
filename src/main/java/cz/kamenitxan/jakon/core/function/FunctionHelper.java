package cz.kamenitxan.jakon.core.function;

import java.util.HashMap;
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


}
