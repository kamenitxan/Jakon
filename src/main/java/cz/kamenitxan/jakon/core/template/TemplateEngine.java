package cz.kamenitxan.jakon.core.template;

import cz.kamenitxan.jakon.core.model.JakonObject;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.model.Post;

import java.util.List;
import java.util.Map;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public interface TemplateEngine {
	void render(String templateName, Map<String, Object> context);
	void renderList(String templateName, Map<String, Object> context);

	default Map<String, Object> getContext(JakonObject jakonObject) {
		return null;
	}
}
