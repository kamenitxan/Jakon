package cz.kamenitxan.jakon.core.model;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.StringWriter;
import java.util.Map;


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
	private String singleTemplate = null;
	@Column
	private String listTemplate = null;
	@Column
	private String sectionName = "";
	@Column
	private boolean published = true;

	public int getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTemplate() {
		return singleTemplate;
	}

	public void setTemplate(String template) {
		this.singleTemplate = template;
	}

	public String getSingleTemplate() {
		return singleTemplate;
	}

	public void setSingleTemplate(String singleTemplate) {
		this.singleTemplate = singleTemplate;
	}

	public String getListTemplate() {
		return listTemplate;
	}

	public void setListTemplate(String listTemplate) {
		this.listTemplate = listTemplate;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

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

	public Map<String, Object> getSingleRenderContext() {
		return null;
	}

	public Map<String, Object> getListRenderContext() {
		return null;
	}
}
