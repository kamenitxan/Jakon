package cz.kamenitxan.jakon.webui.functions;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Template Engine based on Pebble.
 *
 * @author Nikki
 */
public class FixedPebbleTemplateEngine extends TemplateEngine {

	/**
	 * The Pebble Engine instance.
	 */
	private final PebbleEngine engine;

	/**
	 * Construct a new template engine using pebble with a default engine.
	 */
	public FixedPebbleTemplateEngine() {
		this.engine = new PebbleEngine.Builder().build();
	}

	/**
	 * Construct a new template engine using pebble with an engine using a special loader.
	 */
	public FixedPebbleTemplateEngine(Loader loader) {
		this.engine = new PebbleEngine.Builder().loader(loader).extension(new PebbleExtension()).build();
	}

	/**
	 * Construct a new template engine using pebble with a specified engine.
	 *
	 * @param engine The pebble template engine.
	 */
	public FixedPebbleTemplateEngine(PebbleEngine engine) {
		this.engine = engine;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String render(ModelAndView modelAndView) {
		Object model = modelAndView.getModel();

		if (model == null || model instanceof Map) {
			try {
				StringWriter writer = new StringWriter();

				PebbleTemplate template = engine.getTemplate(modelAndView.getViewName());
				if (model == null) {
					template.evaluate(writer);
				} else {
					template.evaluate(writer, (Map<String, Object>) modelAndView.getModel());
				}

				return writer.toString();
			} catch (PebbleException | IOException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Invalid model, model must be instance of Map.");
		}
	}
}
