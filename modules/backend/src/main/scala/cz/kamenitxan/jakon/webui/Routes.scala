package cz.kamenitxan.jakon.webui

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.api.Api
import cz.kamenitxan.jakon.webui.controller.impl.{Authentication, FileManagerController, ObjectController, UserController}
import cz.kamenitxan.jakon.webui.controller.{AbstractController, ExecuteFun}
import cz.kamenitxan.jakon.webui.util.AdminExceptionHandler
import spark.Spark._
import spark.route.HttpMethod
import spark.{Filter, Request, Response, ResponseTransformer}


/**
 * Created by TPa on 03.09.16.
 */
object Routes {
	val AdminPrefix = "/admin"

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
		before(AdminPrefix, new Filter {
			override def handle(request: Request, response: Response): Unit = {
				val user: JakonUser = request.session.attribute("user")
				if ((Settings.getDeployMode ne DeployMode.DEVEL)
				  && user != null
				  && (user.acl.adminAllowed || user.acl.masterAdmin)) {
					response.redirect(s"$AdminPrefix/index", 302)
				}
			}
		})

		before(s"$AdminPrefix/*", new Filter {
			override def handle(req: Request, res: Response): Unit = {
				if (req.pathInfo != s"$AdminPrefix/register"
				  && req.pathInfo != s"$AdminPrefix/logout"
				  && req.pathInfo != s"$AdminPrefix/login"
				  && req.pathInfo != s"$AdminPrefix/resetPassword"
				  && !req.pathInfo.startsWith(s"$AdminPrefix/login/oauth")) {
					var user: JakonUser = req.session.attribute("user")
					if ((Settings.getDeployMode eq DeployMode.DEVEL) && user == null) {
						DBHelper.withDbConnection(implicit conn => {
							user = UserService.getMasterAdmin
							req.session(true).attribute("user", user)
						})
					}
					if (user == null || !user.acl.adminAllowed && !user.acl.masterAdmin) {
						res.redirect(AdminPrefix, 302)
					}
				}
			}
		})


		get(AdminPrefix, (req: Request, _: Response) => Authentication.loginGet(req), te)
		post(AdminPrefix, (req: Request, res: Response) => Authentication.loginPost(req, res), te)

		path(s"$AdminPrefix", () => {
			if (Settings.getDeployMode == DeployMode.PRODUCTION) {
				exception(classOf[Exception], new AdminExceptionHandler)
			}

			get("/index", (request: Request, response: Response) => AdminSettings.dashboardController.apply(request, response), te)
			get("/logout", (req: Request, res: Response) => Authentication.logoutPost(req, res), te)
			get("/profile", (req: Request, res: Response) => UserController.render(req, res), te)
			post("/profile", (req: Request, res: Response) => UserController.update(req, res), te)
			get("/object/:name", (req: Request, res: Response) => ObjectController.getList(req, res), te)
			get("/object/create/:name", (req: Request, res: Response) => ObjectController.getItem(req, res), te)
			post("/object/create/:name", (req: Request, res: Response) => ObjectController.updateItem(req, res), te)
			get("/object/delete/:name/:id", (req: Request, res: Response) => ObjectController.deleteItem(req, res), te)
			get("/object/moveUp/:name/:id", (req: Request, res: Response) => ObjectController.moveInList(req, res, up = true), te)
			get("/object/moveDown/:name/:id", (req: Request, res: Response) => ObjectController.moveInList(req, res, up = false), te)
			get("/object/:name/:id", (req: Request, res: Response) => ObjectController.getItem(req, res), te)
			post("/object/:name/:id", (req: Request, res: Response) => ObjectController.updateItem(req, res), te)
		})
		if (AdminSettings.enableFiles) {
			path(s"$AdminPrefix/files", () => {
				get("/", (req: Request, res: Response) => FileManagerController.getManager(req, res), te)
				get("/frame", (req: Request, res: Response) => FileManagerController.getManagerFrame(req, res), te)
				get("/:method", FileManagerController.executeGet)
				post("/:method", FileManagerController.executePost)
			})
		}

		path(s"$AdminPrefix/api", () => {
			post("/search", (req: Request, res: Response) => Api.search(req, res), gsonTransformer)
			post("/files", (req: Request, _: Response) => Api.getFiles(req, Option.empty), gsonTransformer)
			post("/images", (req: Request, res: Response) => Api.getImages(req, res), gsonTransformer)
		})
		AdminSettings.customControllers.foreach((c: Class[_ <: AbstractController]) => {
			try {
				val instance = c.newInstance
				get(s"$AdminPrefix/" + instance.path, (req: Request, res: Response) => instance.doRender(req, res), te)
				val methods = c.getDeclaredMethods
				for (m <- methods) {
					val an = m.getAnnotation(classOf[ExecuteFun])
					if (an != null) {
						if (!m.isAccessible) m.setAccessible(true)
						an.method match {
							case HttpMethod.get =>
								get(s"$AdminPrefix/" + an.path, (req: Request, res: Response) => m.invoke(instance, req, res).asInstanceOf[Context], te)
							case HttpMethod.post =>
								post(s"$AdminPrefix/" + an.path, (req: Request, _: Response) => m.invoke(instance, req, req).asInstanceOf[Context], te)
							case default => throw new UnsupportedOperationException(default + " is not supported")
						}
					}
				}
				Logger.info("Custom admin controller registered: " + instance.getClass.getSimpleName)
			} catch {
				case e@(_: InstantiationException | _: IllegalAccessException) =>
					Logger.error("Failed to register custom controller", e)
			}
		})
		get(s"$AdminPrefix/*", (req: Request, res: Response) => {
			Logger.warn("Unknown page requested - " + req.url)
			res.status(404)
			new Context(null, "errors/404")
		}, te)
	}

}