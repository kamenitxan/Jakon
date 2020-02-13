package cz.kamenitxan.jakon.webui;

import com.google.gson.Gson;
import cz.kamenitxan.jakon.core.configuration.DeployMode;
import cz.kamenitxan.jakon.core.configuration.Settings;
import cz.kamenitxan.jakon.core.database.DBHelper;
import cz.kamenitxan.jakon.core.model.JakonUser;
import cz.kamenitxan.jakon.core.service.UserService;
import cz.kamenitxan.jakon.webui.api.Api;
import cz.kamenitxan.jakon.webui.controler.AbstractController;
import cz.kamenitxan.jakon.webui.controler.ExecuteFun;
import cz.kamenitxan.jakon.webui.controler.impl.Authentication;
import cz.kamenitxan.jakon.webui.controler.impl.FileManagerControler;
import cz.kamenitxan.jakon.webui.controler.impl.ObjectControler;
import cz.kamenitxan.jakon.webui.controler.impl.UserControler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.TemplateEngine;

import java.lang.reflect.Method;
import java.sql.Connection;

import static spark.Spark.*;

/**
 * Created by TPa on 03.09.16.
 */
// TODO: to scala
public class Routes {
	private static final Logger LOG = LoggerFactory.getLogger(Routes.class);

	public static void init() {
		TemplateEngine te = Settings.getAdminEngine();
		Gson gson = new Gson();


		before("*", ((request, response) -> {
			// also prepares page context
			if (!request.pathInfo().startsWith("/jakon/")) {
				LOG.trace("Processing req: " + request.pathInfo());
			}
		}));
		before("/admin", (request, response) -> {
			JakonUser user = request.session().attribute("user");
			if (Settings.getDeployMode() != DeployMode.DEVEL
					&& request.session().attribute("user") != null
					&& (user.acl().adminAllowed() || user.acl().masterAdmin())) {
				response.redirect("/admin/index", 302);
			}
		});
		before("/admin/*", (req, res) -> {
			if (req.pathInfo().equals("/admin/register")
					|| req.pathInfo().equals("/admin/logout")
					|| req.pathInfo().equals("/admin/login")
					|| req.pathInfo().startsWith("/admin/login/oauth")) {
				return;
			}
			JakonUser user = req.session().attribute("user");
			if (Settings.getDeployMode() == DeployMode.DEVEL && user == null) {
				try (Connection conn = DBHelper.getConnection()) {
					user = UserService.getMasterAdmin(conn);
					req.session(true).attribute("user", user);
				}
			}

			if (user == null || !user.acl().adminAllowed() && !user.acl().masterAdmin()) {
				res.redirect("/admin", 302);
			}
		});

		get("/admin", (req, res) -> Authentication.loginGet(req), te);
		post("/admin", Authentication::loginPost, te);
		get("/admin/index", (request, response) -> AdminSettings.dashboardController().apply(request, response), te);
		get("/admin/logout", Authentication::logoutPost, te);
		get("/admin/profile", UserControler::render, te);
		post("/admin/profile", UserControler::update, te);

		get("/admin/object/:name", ObjectControler::getList, te);
		get("/admin/object/create/:name", ObjectControler::getItem, te);
		post("/admin/object/create/:name", ObjectControler::updateItem, te);
		get("/admin/object/delete/:name/:id", ObjectControler::deleteItem, te);
		get("/admin/object/moveUp/:name/:id", (req, res) -> ObjectControler.moveInList(req, res, true), te);
		get("/admin/object/moveDown/:name/:id", (req, res) -> ObjectControler.moveInList(req, res, false), te);
		get("/admin/object/:name/:id", ObjectControler::getItem, te);
		post("/admin/object/:name/:id", ObjectControler::updateItem, te);

		if (AdminSettings.enableFiles()) {
			path("/admin/files", () -> {
				get("/", FileManagerControler::getManager, te);
				get("/frame", FileManagerControler::getManagerFrame, te);

				get("/:method", FileManagerControler::executeGet);
				post("/:method", FileManagerControler::executePost);
			});
		}


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
							case get:
								get("/admin/" + an.path(), ((req, res) -> (Context) m.invoke(instance, req, res)), te);
								break;
							case post:
								post("/admin/" + an.path(), ((req, res) -> (Context) m.invoke(instance, req, req)), te);
						}
					}
				}
				LOG.info("Custom admin controller registered: " + instance.getClass().getSimpleName());
			} catch (InstantiationException | IllegalAccessException e) {
				LOG.error("Failed to register custom controler", e);
			}

		});

		get("/admin/*", (req, res) -> {
			LOG.warn("Unknown page requested - " + req.url());
			res.status(404);
			return new Context(null, "errors/404");
		}, te);
	}
}
