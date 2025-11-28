package cz.kamenitxan.jakon.webui.entity;

/**
 * Created by TPa on 31.03.18.
 */
public enum HtmlType {
	TEXT("text"),
	DATE("date"),
	TIME("time"),
	DATETIME("datetime-local"),
	SELECT("select"),
	NUMBER("number"),
	CHECKBOX("checkbox");

	String typeName;

	HtmlType(String typeName) {
		this.typeName = typeName;
	}
}
