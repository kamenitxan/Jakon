package cz.kamenitxan.jakon.core.model;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.StringWriter;


/**
 * Created by TPa on 22.04.16.
 */
public class JakonObject {
	@Id
	@GeneratedValue
	private int id;
	@Column
	private String url = "";
	@Column
	private String sectionName = "";
	@Column
	private boolean published = true;

	/**
	 * Return JakonObject Id. Used as primary key in database.
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns relative url. This is used for creating folders for output html files. E.g. "/pages/page-title"
	 * @return relative url
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	/**
	 * Returns true if JakonObject should be rendered, thus published.
	 * @return published state
	 */
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String toJson() {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = Json.createGenerator(writer);

		generator.writeStartObject()
				.write(id)
				.write(url)
		.writeEnd();
		generator.close();
		return writer.toString();
	}

}
