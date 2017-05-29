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

		//staticFiles.externalLocation("/static");

		before("/admin/*", (request, response) -> {
			if (request.session().attribute("user") == null) {
				response.redirect("/admin", 401);
			}
		});



		get("/admin", (request, response) -> Authentication.loginGet(response), te);
		post("/admin", Authentication::loginPost, te);
		get("/admin/index", Dashboard::getDashboard, te);

		get("/admin/object/:name", ObjectControler::getList, te);
		get("/admin/object/create/:name", ObjectControler::getItem, te);
		post("/admin/object/create/:name", ObjectControler::updateItem, te);
		get("/admin/object/delete/:name/:id", ObjectControler::deleteItem, te);
		get("/admin/object/:name/:id", ObjectControler::getItem, te);
		post("/admin/object/:name/:id", ObjectControler::updateItem, te);

	}
}
