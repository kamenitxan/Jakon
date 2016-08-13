package cz.kamenitxan.jakon.core;

import com.mongodb.MongoClient;
import cz.kamenitxan.jakon.core.controler.IControler;
import cz.kamenitxan.jakon.core.controler.PageControler;
import cz.kamenitxan.jakon.core.customPages.CustomPage;
import cz.kamenitxan.jakon.core.model.Dao.MongoHelper;
import cz.kamenitxan.jakon.core.template.Pebble;
import cz.kamenitxan.jakon.core.template.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Director<T extends CustomPage> {
	private static Director instance = null;
	private List<T> customPages = new ArrayList<>();
	private List<IControler> controlers = new ArrayList<>();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Director() {
	}

	public void init() {
		Settings.setTemplateDir("templates/bacon/");
		Settings.setTemplateEngine(new Pebble());
		Settings.setOutputDir("out");
		Settings.setDatabaseDriver("org.sqlite.JDBC");
		Settings.setDatabaseConnPath("jdbc:sqlite:jakon.sqlite");
		MongoHelper.setDbName("jakon");

		registerControler(new PageControler());
	}

	public void render() {
		TemplateUtils.clean(Settings.getOutputDir());

		controlers.parallelStream().forEach(IControler::generate);
		customPages.parallelStream().forEach(CustomPage::render);

		if (Settings.getStaticDir() != null && Settings.getOutputDir() != null) {
			TemplateUtils.copy(Settings.getStaticDir(), Settings.getOutputDir());
		}
		logger.info("Render complete");
	}

	public void registerCustomPage(T page) {
		customPages.add(page);
	}

	public void removeCustomPage(T page) {
		customPages.remove(page);
	}

	public void registerControler(IControler controler) {
		controlers.add(controler);
	}

	public static synchronized Director getInstance() {
		if (instance == null) {
			instance = new Director();
		}
		return instance;
	}
}
