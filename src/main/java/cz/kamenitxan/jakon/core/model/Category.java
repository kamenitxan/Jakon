package cz.kamenitxan.jakon.core.model;


import cz.kamenitxan.jakon.webui.ObjectSettings;
import cz.kamenitxan.jakon.webui.entity.JakonField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
@Entity
public class Category extends JakonObject {
	@Column
	@JakonField(searched = true)
	private String name;
	@Column
	@JakonField
	private boolean showComments;

	public Category() {
		super(Category.class.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isShowComments() {
		return showComments;
	}

	public void setShowComments(boolean showComments) {
		this.showComments = showComments;
	}

	@Override
	public ObjectSettings objectSettings() {
		return null;
	}

	@Override
	public String toString() {
		return "Category{" +
				"id='" + id() + '\'' +
				", name='" + name + '\'' +
				"} ";
	}
}
