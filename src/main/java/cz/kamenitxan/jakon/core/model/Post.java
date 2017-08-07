package cz.kamenitxan.jakon.core.model;

import cz.kamenitxan.jakon.core.function.FunctionHelper;
import cz.kamenitxan.jakon.webui.ObjectSettings;
import cz.kamenitxan.jakon.webui.entity.JakonField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Post extends JakonObject {
	@Column
	@JakonField
	private Date date;
	@Column
	@JakonField(required = false)
	private String perex;
	@Column
	@ManyToOne
	@JakonField(required = false, inputTemplate = "String")
	private Category category;
	@Column
	@JakonField
	private String title;
	@Column
	@JakonField
	private String content;
	@Column
	@JakonField
	private boolean showComments = false;


	public Post() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		if (content == null) {
			return null;
		}
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

	public boolean isShowComments() {
		return showComments;
	}

	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getPerex() {
		return perex;
	}

	public void setPerex(String perex) {
		this.perex = perex;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public ObjectSettings objectSettings() {
		return null;
	}
}
