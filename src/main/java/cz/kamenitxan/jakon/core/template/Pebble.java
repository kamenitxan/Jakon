package cz.kamenitxan.jakon.core.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import cz.kamenitxan.jakon.core.model.JakonObject;
import cz.kamenitxan.jakon.core.model.Page;
import cz.kamenitxan.jakon.core.model.Post;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Pebble implements TemplateEngine {
	private static PebbleEngine engine;

	static {
		Loader loader = new FileLoader();
		loader.setPrefix("templates/bacon/");
		loader.setSuffix(".peb");
		engine = new PebbleEngine(loader);
	}

	@Override
	public void render(String templateName, Map<String, Object> context) {
		PebbleTemplate compiledTemplate = null;
		try {
			compiledTemplate = engine.getTemplate(templateName);
		} catch (PebbleException e) {
			e.printStackTrace();
		}

		Writer writer = new StringWriter();
		try {
			if (compiledTemplate != null) {
				compiledTemplate.evaluate(writer, context);
			}
		} catch (PebbleException | IOException e) {
			e.printStackTrace();
		}

		String output = writer.toString();
	}

	@Override
	public void renderList(String templateName, Map<String, Object> context) {
		PebbleTemplate compiledTemplate = null;
		try {
			compiledTemplate = engine.getTemplate(templateName);
		} catch (PebbleException e) {
			e.printStackTrace();
		}

		Writer writer = new StringWriter();
		try {
			if (compiledTemplate != null) {
				compiledTemplate.evaluate(writer, context);
			}
		} catch (PebbleException | IOException e) {
			e.printStackTrace();
		}

		String output = writer.toString();
		TemplateUtils.saveRenderedPage(templateName, output, "");
	}

}
