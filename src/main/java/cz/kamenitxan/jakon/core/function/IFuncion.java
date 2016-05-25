package cz.kamenitxan.jakon.core.function;

import java.util.Map;

/**
 * Created by TPa on 25.05.16.
 */
public interface IFuncion {
	default String getName() {
		return this.getClass().getSimpleName();
	}

	String execute(Map<String, String> params);
}
