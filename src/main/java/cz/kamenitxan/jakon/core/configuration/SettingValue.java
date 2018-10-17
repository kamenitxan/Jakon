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
	DEFAULT_LOCALE("defaultLocale"),
	MAIL_AUTH("MAIL.auth", false, "false"),
	MAIL_TLS("MAIL.tls", false, "false"),
	MAIL_HOST("MAIL.host", false, "localhost"),
	MAIL_PORT("MAIL.port", false, "25"),
	MAIL_USERNAME("MAIL.username", false, ""),
	MAIL_PASSWORD("MAIL.password", false, "");


	public String name;
	public boolean required;
	public String defaultValue;

	SettingValue(String name) {
		this.name = name;
		this.required = false;
	}

	SettingValue(String name, boolean required) {
		this.name = name;
		this.required = required;
	}

	SettingValue(String name, boolean required, String defaultValue) {
		this.name = name;
		this.required = required;
		this.defaultValue = defaultValue;
	}

	public static SettingValue fromName(String name) {
		for (SettingValue v : SettingValue.values()) {
			if (v.name.equals(name)) return v;
		}
		throw new IllegalArgumentException("Uknown setting");
	}

}
