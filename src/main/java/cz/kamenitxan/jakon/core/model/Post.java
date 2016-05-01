package cz.kamenitxan.jakon.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Post extends Page{
	@Column
	private Date date;
	@Column
	private String perex;
	@Column
	@ManyToOne
	private Category category;

	public Post() {
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
