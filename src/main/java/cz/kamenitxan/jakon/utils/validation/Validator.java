package cz.kamenitxan.jakon.utils.validation;

/**
 * Created by TPa on 11.08.16.
 */
public interface Validator {
	boolean isValid(Object... params);

	default boolean allValid(Object... params) {
		if (params == null) return false;
		for (Object o : params) {
			if (!isValid(o)) return false;
		}
		return true;
	}
}
