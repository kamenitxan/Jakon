package cz.kamenitxan.jakon.core.template;

import java.util.Map;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public interface TemplateEngine {
	void render(String templateName, String path, Map<String, Object> context);
}
