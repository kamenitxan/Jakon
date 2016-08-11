package cz.kamenitxan.jakon.utils.validation.validators;

import cz.kamenitxan.jakon.utils.validation.Validator;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Created by TPa on 11.08.16.
 */
public class NotEmptyValidator implements Validator {
	@Override
	public boolean isValid(Object... params) {
		if (params == null ) {
			return false;
		}

		final Object value = params[0];
		if (value.getClass().isArray()) {
			return Array.getLength(value) > 0;
		}
		if (value instanceof Collection) {
			return !((Collection<?>) value).isEmpty();
		}
		if (value instanceof Map) {
			return !((Map<?, ?>) value).isEmpty();
		}
		if (value instanceof String) {
			return !((String) value).trim().isEmpty();
		}
		return true;
	}
}
