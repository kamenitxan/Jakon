package cz.kamenitxan.jakon.core.model;

import cz.kamenitxan.jakon.core.function.FunctionHelper;
import cz.kamenitxan.jakon.core.template.TemplateUtils;
import cz.kamenitxan.jakon.webui.ObjectSettings;
import cz.kamenitxan.jakon.webui.entity.JakonField;
import scala.NotImplementedError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Connection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Post extends JakonObject {

	@Column
	@JakonField(listOrder = 4)
	private Date date;
	@Column
	@JakonField(required = false, listOrder = 2)
	private String perex;
	@ManyToOne
	@JakonField(required = false, inputTemplate = "String", listOrder = 5)
	private Category category;
	@Column
	@JakonField(listOrder = 1, searched = true)
	private String title;
	@Column
	@JakonField(inputTemplate = "textarea", listOrder = 3)
	private String content;
	@Column
	@JakonField(listOrder = 6)
	private boolean showComments = false;


	public Post() {
		super(Post.class.getName());
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

		String parsedHtml = TemplateUtils.parseMarkdown(content);

		// TODO: parsovani funkci
		// (\{)((?:[a-z][a-z]+)).*?(\})

		Pattern p = Pattern.compile("(\\{)((?:[a-z][a-z]+))(.*?)(\\})");
		Matcher m = p.matcher(parsedHtml);

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

	@Override
	public int createObject(int jid, Connection conn) {
		throw new NotImplementedError();
	}

	@Override
	public void updateObject(int jid, Connection conn) {
		throw new NotImplementedError();
	}
}
