package cz.kamenitxan.jakon.core.configuration;

import com.mitchellbosecke.pebble.loader.FileLoader;
import cz.kamenitxan.jakon.core.model.DeployMode;
import cz.kamenitxan.jakon.core.template.Pebble;
import cz.kamenitxan.jakon.core.template.TemplateEngine;
import cz.kamenitxan.jakon.core.template.FixedPebbleTemplateEngine;
import cz.kamenitxan.jakon.webui.util.JakonFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public abstract class Settings {
	private static Logger logger = LoggerFactory.getLogger(Settings.class);
	private static TemplateEngine engine;
	private static spark.TemplateEngine adminEngine;
	private static Map<SettingValue, String> settings = new HashMap<>();

	static {
		try {
			init(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
			String value = prop.getProperty(key).trim();
			try {
				settings.put(SettingValue.fromName(key), value);
			} catch (IllegalArgumentException ignored) {
				logger.error("Cant load setting value", e);
			}

		}

		FileLoader loader = new JakonFileLoader();
		loader.setSuffix(".peb");
		adminEngine = new FixedPebbleTemplateEngine(loader);
		setTemplateEngine(new Pebble());
	}

	public static String getTemplateDir() {
		return settings.get(SettingValue.TEMPLATE_DIR);
	}

	public static void setTemplateDir(String templateDir) {
		settings.put(SettingValue.TEMPLATE_DIR, templateDir);
	}

	public static TemplateEngine getTemplateEngine() {
		return engine;
	}

	public static void setTemplateEngine(TemplateEngine engine) {
		Settings.engine = engine;
	}

	public static spark.TemplateEngine getAdminEngine() {
		return adminEngine;
	}

	public static void setAdminEngine(spark.TemplateEngine adminEngine) {
		Settings.adminEngine = adminEngine;
	}

	public static String getStaticDir() {
		return settings.get(SettingValue.STATIC_DIR);
	}

	public static void setStaticDir(String staticDir) {
		settings.put(SettingValue.STATIC_DIR, staticDir);
	}

	public static String getOutputDir() {
		return settings.get(SettingValue.OUTPUT_DIR);
	}

	public static void setOutputDir(String outputDir) {
		settings.put(SettingValue.OUTPUT_DIR, outputDir);
	}

	public static String getDatabaseDriver() {
		return settings.get(SettingValue.DB_DRIVER);
	}

	public static void setDatabaseDriver(String databaseDriver) {
		settings.put(SettingValue.DB_DRIVER, databaseDriver);
	}

	public static String getDatabaseConnPath() {
		return settings.get(SettingValue.DB_URL);
	}

	public static void setDatabaseConnPath(String databaseConnPath) {
		settings.put(SettingValue.DB_URL, databaseConnPath);
	}

	public static int getPort() {
		return Integer.valueOf(settings.get(SettingValue.PORT));
	}

	public static void setPort(int port) {
		settings.put(SettingValue.PORT, String.valueOf(port));
	}

	public static String getProperty(SettingValue name) {
		return settings.get(name);
	}

	public static DeployMode getDeployMode() {
		String mode = settings.get(SettingValue.DEPLOY_MODE);
		if (mode != null) {
			return DeployMode.valueOf(mode);
		} else {
			return DeployMode.PRODUCTION;
		}
	}

	public static void setDeployMode(DeployMode mode) {
		settings.put(SettingValue.DEPLOY_MODE, mode.name());
	}
}
