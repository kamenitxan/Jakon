package cz.kamenitxan.jakon.core;

import cz.kamenitxan.jakon.core.template.TemplateEngine;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public abstract class Settings {
	private static TemplateEngine engine;

	private static Map<String, String> settings = new HashMap<>();

	public static void init(File configFile) throws IOException {
		if (configFile == null) {
			configFile = new File("jakon_config.properties");
		}
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);

		Enumeration<?> e = prop.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = prop.getProperty(key);
			settings.put(key, value);
		}
	}

	public static String getTemplateDir() {
		return settings.get("templateDir");
	}

	public static void setTemplateDir(String templateDir) {
		settings.put("templateDir", templateDir);
	}

	public static TemplateEngine getTemplateEngine() {
		return engine;
	}

	public static void setTemplateEngine(TemplateEngine engine) {
		Settings.engine = engine;
	}

	public static String getStaticDir() {
		return settings.get("staticDir");
	}

	public static void setStaticDir(String staticDir) {
		settings.put("staticDir", staticDir);
	}

	public static String getOutputDir() {
		return settings.get("outputDir");
	}

	public static void setOutputDir(String outputDir) {
		settings.put("outputDir", outputDir);
	}

	public static String getDatabaseDriver() {
		return settings.get("databaseDriver");
	}

	public static void setDatabaseDriver(String databaseDriver) {
		settings.put("databaseDriver", databaseDriver);
	}

	public static String getDatabaseConnPath() {
		return settings.get("databaseConnPath");
	}

	public static void setDatabaseConnPath(String databaseConnPath) {
		settings.put("databaseConnPath", databaseConnPath);
	}

	public static int getPort() {
		return Integer.valueOf(settings.get("port"));
	}

	public static void setPort(int port) {
		settings.put("port", String.valueOf(port));
	}
}
