package cz.kamenitxan.jakon.webui;

import cz.kamenitxan.jakon.core.Settings;
import cz.kamenitxan.jakon.core.model.DeployMode;
import cz.kamenitxan.jakon.webui.controler.FileManagerControler;
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
			if (Settings.getDeployMode() == DeployMode.DEVEL) return;
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

		path("/admin/files", () -> {
			get("/", FileManagerControler::getManager, te);

			get("/:method", FileManagerControler::executeGet);
			post("/:method", FileManagerControler::executePost);

			/*post("/listUrl", FileManagerControler::getManager, te);
			post("/uploadUrl", FileManagerControler::getManager, te);
			post("/renameUrl", FileManagerControler::getManager, te);
			post("/copyUrl", FileManagerControler::getManager, te);
			post("/moveUrl", FileManagerControler::getManager, te);
			post("/removeUrl", FileManagerControler::getManager, te);
			post("/editUrl", FileManagerControler::getManager, te);
			post("/getContentUrl", FileManagerControler::getManager, te);
			post("/createFolderUrl", FileManagerControler::getManager, te);
			post("/downloadFileUrl", FileManagerControler::getManager, te);
			post("/downloadMultipleUrl", FileManagerControler::getManager, te);
			post("/compressUrl", FileManagerControler::getManager, te);
			post("/extractUrl", FileManagerControler::getManager, te);
			post("/permissionsUrl", FileManagerControler::getManager, te);
			post("/basePath", FileManagerControler::getManager, te);*/
		});
	}
}
