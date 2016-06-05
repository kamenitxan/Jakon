package cz.kamenitxan.jakon.core;

import cz.kamenitxan.jakon.core.template.TemplateEngine;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public abstract class Settings {
	private static String templateDir;
	private static TemplateEngine engine;
	private static String staticDir;
	private static String outputDir;
	private static String databaseDriver;
	private static String databaseConnPath;
	private static int port = 4567;

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

	public static String getStaticDir() {
		return staticDir;
	}

	public static void setStaticDir(String staticDir) {
		Settings.staticDir = staticDir;
	}

	public static String getOutputDir() {
		return outputDir;
	}

	public static void setOutputDir(String outputDir) {
		Settings.outputDir = outputDir;
	}

	public static String getDatabaseDriver() {
		return databaseDriver;
	}

	public static void setDatabaseDriver(String databaseDriver) {
		Settings.databaseDriver = databaseDriver;
	}

	public static String getDatabaseConnPath() {
		return databaseConnPath;
	}

	public static void setDatabaseConnPath(String databaseConnPath) {
		Settings.databaseConnPath = databaseConnPath;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Settings.port = port;
	}
}
