package cz.kamenitxan.jakon.core.model;

import javax.persistence.*;

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
		return content;
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
}
