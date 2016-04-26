package cz.kamenitxan.jakon.core.model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
		setListTemplate("list");
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
	public Map<String, Object> getSingleRenderContext() {
		Map<String, Object> context = new HashMap<>();
		return super.getSingleRenderContext();
	}

	@Override
	public Map<String, Object> getListRenderContext() {
		return super.getListRenderContext();
	}
}
