package cz.kamenitxan.jakon.webui;

import cz.kamenitxan.jakon.core.Settings;
import cz.kamenitxan.jakon.webui.controler.ObjectControler;
import spark.TemplateEngine;

import static spark.Spark.*;

/**
 * Created by TPa on 03.09.16.
 */
public class Routes {

	public static void init() {
		TemplateEngine te = Settings.getAdminEngine();

		externalStaticFileLocation("out");

		get("/admin", (request, response) -> Authentication.loginGet(response), te);
		get("/admin/index", Dashboard::getDashboard, te);

		get("/admin/object/:name", ObjectControler::getList, te);
		get("/admin/object/:name/:id", ObjectControler::getItem, te);
		post("/admin/object/:name/:id", ObjectControler::updateItem, te);
	}
}
