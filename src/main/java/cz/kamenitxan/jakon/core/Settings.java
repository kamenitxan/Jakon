package cz.kamenitxan.jakon.core;

import cz.kamenitxan.jakon.core.template.TemplateEngine;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Settings {
	private static String templateDir;
	private static TemplateEngine engine;

	public static String getTemplateDir() {
		return templateDir;
	}

	public static void setTemplateDir(String templateDir) {
		Settings.templateDir = templateDir;
	}

	public static TemplateEngine getTemplateEngine() {
		return engine;
	}

	public static void setTemplateEngine(TemplateEngine engine) {
		Settings.engine = engine;
	}
}
