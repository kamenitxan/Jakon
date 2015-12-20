package cz.kamenitxan.jakon.core.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Page {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String title;
	@Column
	private String content;

	public Page() {
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
