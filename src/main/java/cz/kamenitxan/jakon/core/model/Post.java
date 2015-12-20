package cz.kamenitxan.jakon.core.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
public class Post extends Page {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private Date date;
	@Column
	private String perex;
	@Column
	private Category category;

	public Post() {
	}

	public int getId() {
		return id;
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
}
