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
	void render(String templateName, JakonObject jakonObject);
	void render(String templateName, List<? extends JakonObject> jakonObjects);

	default Map<String, Object> getContext(JakonObject jakonObject) {
		return null;
	}
}
