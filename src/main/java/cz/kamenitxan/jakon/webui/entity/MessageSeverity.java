package cz.kamenitxan.jakon.webui.entity;

public enum MessageSeverity {
	SUCCESS("alert-success"),
	INFO("alert-info"),
	WARNING("alert-warning"),
	ERROR("alert-danger");

	public String value;

	MessageSeverity(String value) {
		this.value = value;
	}
}
