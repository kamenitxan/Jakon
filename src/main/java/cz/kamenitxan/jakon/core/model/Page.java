package cz.kamenitxan.jakon.core.model;

import cz.kamenitxan.jakon.core.function.FunctionHelper;
import cz.kamenitxan.jakon.webui.ObjectSettings;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.*;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Page extends JakonObject {
	@Column
	private String title;
	@Column
	private String content;
	@Column
	@OneToOne
	private Page parent = null;
	@Column
	private boolean showComments = false;

	public Page() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		// TODO: parsovani funkci
		// (\{)((?:[a-z][a-z]+)).*?(\})

		Pattern p = Pattern.compile("(\\{)((?:[a-z][a-z]+))(.*?)(\\})");
		Matcher m = p.matcher(content);

		StringBuffer result = new StringBuffer();
		while (m.find()) {
			String funcion = m.group(1);
			String params = m.group(2);
			m.appendReplacement(result, FunctionHelper.getFunction(funcion).execute(FunctionHelper.splitParams(params)));
		}
		m.appendTail(result);
		return result.toString();
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Page getParent() {
		return parent;
	}

	public void setParent(Page parent) {
		this.parent = parent;
	}

	public boolean isShowComments() {
		return showComments;
	}

	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}

	@Override
	public String toJson() {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = Json.createGenerator(writer);

		generator.writeStartObject()
				.write(super.getId())
				.write(getTitle())
				.write(getContent())
				.write(getParent().getId())
				.writeEnd();
		generator.close();
		return writer.toString();
	}

	@Override
	public ObjectSettings objectSettings() {
		return null;
	}
}
