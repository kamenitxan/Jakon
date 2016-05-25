package cz.kamenitxan.jakon.core.model;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.*;
import java.io.StringWriter;

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
}
