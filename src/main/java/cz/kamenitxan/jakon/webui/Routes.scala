package cz.kamenitxan.jakon.webui

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.api.Api
import cz.kamenitxan.jakon.webui.controller.{AbstractController, ExecuteFun}
import cz.kamenitxan.jakon.webui.controller.impl.{Authentication, FileManagerController, ObjectController, UserController}
import spark.Spark._
import spark.route.HttpMethod
import spark.{Filter, Request, Response, ResponseTransformer}


/**
 * Created by TPa on 03.09.16.
 */
object Routes {

	def init(): Unit = {
		val te = Settings.getAdminEngine
		val gson = new Gson
		val gsonTransformer: ResponseTransformer = (model: Any) => gson.toJson(model)

		before("*", new Filter {
			override def handle(request: Request, response: Response): Unit = {
				// also prepares page context
				if (!request.pathInfo.startsWith("/jakon/")) {
					Logger.debug("Processing req: " + request.pathInfo)
				}
			}
		})
		before("/admin", new Filter {
			override def handle(request: Request, response: Response): Unit = {
				val user: JakonUser = request.session.attribute("user")
				if ((Settings.getDeployMode ne DeployMode.DEVEL)
				  && request.session.attribute("user") != null
				  && (user.acl.adminAllowed || user.acl.masterAdmin)) {
					response.redirect("/admin/index", 302)
				}
			}
		})

		before("/admin/*", new Filter {
			override def handle(req: Request, res: Response): Unit = {
				if (req.pathInfo != "/admin/register"
				  && req.pathInfo != "/admin/logout"
				  && req.pathInfo != "/admin/login"
				  && !req.pathInfo.startsWith("/admin/login/oauth")) {
					var user: JakonUser = req.session.attribute("user")
					if ((Settings.getDeployMode eq DeployMode.DEVEL) && user == null) {
						DBHelper.withDbConnection(implicit conn => {
							user = UserService.getMasterAdmin
							req.session(true).attribute("user", user)
						})
					}
					if (user == null || !user.acl.adminAllowed && !user.acl.masterAdmin) {
						res.redirect("/admin", 302)
					}
				}
			}
		})


		get("/admin", (req: Request, _: Response) => Authentication.loginGet(req), te)
		post("/admin", (req: Request, res: Response) => Authentication.loginPost(req, res), te)
		get("/admin/index", (request: Request, response: Response) => AdminSettings.dashboardController.apply(request, response), te)
		get("/admin/logout", (req: Request, res: Response) => Authentication.logoutPost(req, res), te)
		get("/admin/profile", (req: Request, res: Response) => UserController.render(req, res), te)
		post("/admin/profile", (req: Request, res: Response) => UserController.update(req, res), te)
		get("/admin/object/:name", (req: Request, res: Response) => ObjectController.getList(req, res), te)
		get("/admin/object/create/:name", (req: Request, res: Response) => ObjectController.getItem(req, res), te)
		post("/admin/object/create/:name", (req: Request, res: Response) => ObjectController.updateItem(req, res), te)
		get("/admin/object/delete/:name/:id", (req: Request, res: Response) => ObjectController.deleteItem(req, res), te)
		get("/admin/object/moveUp/:name/:id", (req: Request, res: Response) => ObjectController.moveInList(req, res, up = true), te)
		get("/admin/object/moveDown/:name/:id", (req: Request, res: Response) => ObjectController.moveInList(req, res, up = false), te)
		get("/admin/object/:name/:id", (req: Request, res: Response) => ObjectController.getItem(req, res), te)
		post("/admin/object/:name/:id", (req: Request, res: Response) => ObjectController.updateItem(req, res), te)
		if (AdminSettings.enableFiles) {
			path("/admin/files", () => {
				get("/", (req: Request, res: Response) => FileManagerController.getManager(req, res), te)
				get("/frame", (req: Request, res: Response) => FileManagerController.getManagerFrame(req, res), te)
				get("/:method", FileManagerController.executeGet)
				post("/:method", FileManagerController.executePost)
			})
		}

		path("/admin/api", () => {
			post("/search", (req: Request, res: Response) => Api.search(req, res), gsonTransformer)
		})
		AdminSettings.customControllersJava.forEach((c: Class[_ <: AbstractController]) => {
			try {
				val instance = c.newInstance
				get("/admin/" + instance.path, (req: Request, res: Response) => instance.doRender(req, res), te)
				val methods = c.getDeclaredMethods
				for (m <- methods) {
					val an = m.getAnnotation(classOf[ExecuteFun])
					if (an != null) {
						if (!m.isAccessible) m.setAccessible(true)
						an.method match {
							case HttpMethod.get =>
								get("/admin/" + an.path, (req: Request, res: Response) => m.invoke(instance, req, res).asInstanceOf[Context], te)
							case HttpMethod.post =>
								post("/admin/" + an.path, (req: Request, _: Response) => m.invoke(instance, req, req).asInstanceOf[Context], te)
						}
					}
				}
				Logger.info("Custom admin controller registered: " + instance.getClass.getSimpleName)
			} catch {
				case e@(_: InstantiationException | _: IllegalAccessException) =>
					Logger.error("Failed to register custom controller", e)
			}
		})
		get("/admin/*", (req: Request, res: Response) => {
			Logger.warn("Unknown page requested - " + req.url)
			res.status(404)
			new Context(null, "errors/404")
		}, te)
	}

}