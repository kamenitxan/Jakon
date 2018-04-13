package cz.kamenitxan.jakon.webui.entity;

/**
 * Created by TPa on 31.03.18.
 */
public enum HtmlType {
	TEXT("test"),
	DATE("date"),
	DATETIME("datetime-local"),
	SELECT("select"),
	CHECKBOX("checkbox");

	String typeName;

	HtmlType(String typeName) {
		this.typeName = typeName;
	}
}
