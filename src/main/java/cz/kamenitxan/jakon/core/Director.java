package cz.kamenitxan.jakon.core;

import com.j256.ormlite.dao.Dao;
import cz.kamenitxan.jakon.core.controler.IControler;
import cz.kamenitxan.jakon.core.controler.PageControler;
import cz.kamenitxan.jakon.core.customPages.CustomPage;
import cz.kamenitxan.jakon.core.function.Link;
import cz.kamenitxan.jakon.core.model.Dao.DBHelper;
import cz.kamenitxan.jakon.core.model.Post;
import cz.kamenitxan.jakon.core.template.Pebble;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Director<T extends CustomPage> {
	private static Director instance = null;
	private List<T> customPages = new ArrayList<>();
	private List<IControler> controlers = new ArrayList<>();

	private Director() {
	}

	public void init() {
		Settings.setTemplateDir("templates/bacon/");
		Settings.setTemplateEngine(new Pebble());

		registerControler(new PageControler());
	}

	public void render() {
		controlers.parallelStream().forEach( a -> render());
		customPages.parallelStream().forEach( a -> render());
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
			new Director();
		}
		return instance;
	}
}
