package cz.kamenitxan.jakon.webui;

import com.google.gson.Gson;
import cz.kamenitxan.jakon.core.configuration.Settings;
import cz.kamenitxan.jakon.core.model.DeployMode;
import cz.kamenitxan.jakon.core.model.JakonUser;
import cz.kamenitxan.jakon.webui.api.Api;
import cz.kamenitxan.jakon.webui.controler.AbstractController;
import cz.kamenitxan.jakon.webui.controler.ExecuteFun;
import cz.kamenitxan.jakon.webui.controler.impl.Authentication;
import cz.kamenitxan.jakon.webui.controler.impl.FileManagerControler;
import cz.kamenitxan.jakon.webui.controler.impl.ObjectControler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.TemplateEngine;

import java.lang.reflect.Method;

import static spark.Spark.*;

/**
 * Created by TPa on 03.09.16.
 */
public class Routes {
	private static Logger logger = LoggerFactory.getLogger(Routes.class);
	public static void init() {
		TemplateEngine te = Settings.getAdminEngine();
		Gson gson = new Gson();

		before("/admin/*", (request, response) -> {
			if (Settings.getDeployMode() == DeployMode.DEVEL || request.pathInfo().equals("/admin/register")) return;
			JakonUser user = request.session().attribute("user");
			if (request.session().attribute("user") == null || !user.acl().adminAllowed() && !user.acl().masterAdmin()) {
				response.redirect("/admin", 401);
			}
		});

		get("/admin", (request, response) -> Authentication.loginGet(response), te);
		post("/admin", Authentication::loginPost, te);
		get("/admin/index", (request, response) -> AdminSettings.dashboardController().apply(request, response), te);
		get("/admin/register",  (request, response) -> Authentication.registrationGet(response), te);
		post("/admin/register", Authentication::registrationPost, te);

		get("/admin/object/:name", ObjectControler::getList, te);
		get("/admin/object/create/:name", ObjectControler::getItem, te);
		post("/admin/object/create/:name", ObjectControler::updateItem, te);
		get("/admin/object/delete/:name/:id", ObjectControler::deleteItem, te);
		get("/admin/object/:name/:id", ObjectControler::getItem, te);
		post("/admin/object/:name/:id", ObjectControler::updateItem, te);

		if (AdminSettings.enableFiles()) {
			path("/admin/files", () -> {
				get("/", FileManagerControler::getManager, te);
				get("/frame", FileManagerControler::getManagerFrame, te);

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
		/*if (AdminSettings.enableDeploy()) {
			path("/admin/deploy", () -> {
				//get("/", DeployControler::getOverview, te);
				//get("/start", DeployControler::deploy, te);
			});
		}*/

		path("/admin/api", () -> {
			post("/search", Api::search, gson::toJson);
		});

		AdminSettings.customControllersJava().forEach(c -> {
			try {
				AbstractController instance = c.newInstance();
				get("/admin/" + instance.path(), instance::doRender, te);

				Method[] methods = c.getDeclaredMethods();
				for (Method m : methods) {
					ExecuteFun an = m.getAnnotation(ExecuteFun.class);
					if (an != null) {
						m.setAccessible(true);
						switch (an.method()) {
							case get: get("/admin/" + an.path(), ((req, res) -> (Context) m.invoke(instance, req, res)) , te);
							case post: post("/admin/" + an.path(), ((req, res) -> (Context) m.invoke(instance, req, req)) , te);
						}
					}
				}
				logger.info("Custom admin controller registered: " + instance.getClass().getSimpleName());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}

		});
	}
}
