package cz.kamenitxan.jakon.utils.validation.validators;

import cz.kamenitxan.jakon.utils.validation.Validator;

/**
 * Created by TPa on 11.08.16.
 */
public class NotNullValidator implements Validator {
	@Override
	public boolean isValid(Object... param) {
		return param != null;
	}
}
