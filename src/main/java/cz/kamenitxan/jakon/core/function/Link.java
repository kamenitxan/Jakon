package cz.kamenitxan.jakon.core.function;

import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.JakonObject;

import java.util.Map;

/**
 * Created by TPa on 25.05.16.
 */
public class Link implements IFuncion {

	@Override
	public String execute(Map<String, String> params) {
		Integer objectId = Integer.valueOf(params.getOrDefault("id", null));
		String target = params.getOrDefault("target", null);
		String text = params.getOrDefault("text", null);

		if (objectId == null || text == null) {
			throw new IllegalArgumentException("Invalid link parameters");
		}

		JakonObject object = DBHelper.getObjectById(objectId);

		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(object.getUrl());
		sb.append("\" ");
		if (target != null) {
			sb.append("target=\"").append(target).append("\" ");
		}
		sb.append(">").append(text).append("</a>");
 		return sb.toString();
	}
}
