package cz.kamenitxan.jakon.webui.entity;

import org.openqa.selenium.InvalidArgumentException;

import java.util.Arrays;

/**
 * Created by TPa on 2019-07-06.
 */
public
enum FileManagerMode {
	LIST("list"),
	RENAME("rename"),
	MOVE("move"),
	COPY("copy"),
	REMOVE("remove"),
	EDIT("edit"),
	GET_CONTENT("getContent"),
	CREATE_FOLDER("createFolder"),
	CHANGE_PERMISSIONS("changePermissions"),
	COMPRESS("compress"),
	EXTRACT("extract"),
	UPLOAD("upload");

	final String action;

	FileManagerMode(String action) {
		this.action = action;
	}

	public static FileManagerMode ofAction(String action) {
		return Arrays
				.stream(FileManagerMode.values())
				.filter(fm -> fm.action.equals(action))
				.findAny()
				.orElseThrow(() -> new InvalidArgumentException("Unknown FileManagerMode"));
	}
}
