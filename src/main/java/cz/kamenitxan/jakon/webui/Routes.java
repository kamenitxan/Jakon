package cz.kamenitxan.jakon.webui;

import cz.kamenitxan.jakon.core.Settings;
import spark.TemplateEngine;

import static spark.Spark.*;

/**
 * Created by TPa on 03.09.16.
 */
public class Routes {

	public static void init() {
		TemplateEngine te = Settings.getAdminEngine();

		externalStaticFileLocation("out");

		get("/admin", ((request, response) -> Authentication.loginGet(response)), te);


	}
}
