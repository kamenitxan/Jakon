package cz.kamenitxan.jakon.core.configuration;

public enum SettingValue {
	TEMPLATE_DIR("templateDir", true),
	STATIC_DIR("staticDir", true),
	OUTPUT_DIR("outputDir", true),
	DB_DRIVER("databaseDriver", true),
	DB_URL("databaseConnPath", true),
	DB_USER("databaseUser"),
	DB_PASS("databasePass"),
	PORT("port", true),
	DEPLOY_MODE("deployMode", true),
	DEPLOY_TYPE("DEPLOY.type"),
	DEFAULT_LOCALE("defaultLocale");


	public String name;
	public boolean required;

	SettingValue(String name) {
		this.name = name;
		this.required = false;
	}

	SettingValue(String name, boolean required) {
		this.name = name;
		this.required = required;
	}

	public static SettingValue fromName(String name) {
		for (SettingValue v : SettingValue.values()) {
			if (v.name.equals(name)) return v;
		}
		throw new IllegalArgumentException("Uknown setting");
	}

}
