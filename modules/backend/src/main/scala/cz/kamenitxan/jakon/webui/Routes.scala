package cz.kamenitxan.jakon.webui

import com.google.gson.GsonBuilder
import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.gson.*
import cz.kamenitxan.jakon.webui.api.Api
import cz.kamenitxan.jakon.webui.controller.impl.{Authentication, FileManagerController, ObjectController, UserController}
import cz.kamenitxan.jakon.webui.controller.{AbstractController, ExecuteFun}
import cz.kamenitxan.jakon.webui.util.AdminExceptionHandler
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.{Context, Handler, HttpStatus}

import java.time.{LocalDateTime, ZonedDateTime}
import scala.jdk.CollectionConverters.*


/**
 * Created by TPa on 03.09.16.
 */
object Routes {
	val AdminPrefix = "/admin"
	private val te = Settings.getAdminEngine

	def init(): Unit = {
		if (Settings.isInitRoutes) {
			initRoutes()
		}
	}

	private def render(ctx: Context, modelAndView: ModelAndView): String = {
		if (modelAndView != null) {
			te.render(modelAndView.getViewName, modelAndView.getModel.asInstanceOf[java. util. Map[String, ?]], ctx)
		} else {
			""
		}
	}

	private def initRoutes(): Unit = {

		val gson = new GsonBuilder()
			.registerTypeAdapter(classOf[Option[Any]], new GsonOptionSerializer[Any])
			.registerTypeAdapter(classOf[Seq[Any]], new GsonSeqSerializer[Any])
			.registerTypeAdapter(classOf[Map[Any, Any]], new GsonMapSerializer[Any, Any])
			.registerTypeAdapter(classOf[ZonedDateTime], new GsonZonedDateTimeDeserializer)
			.registerTypeAdapter(classOf[ZonedDateTime], new GsonZonedDateTimeSerializer)
			.registerTypeAdapter(classOf[LocalDateTime], new GsonLocalDateTimeSerializer)
			.create()
		//val gsonTransformer: ResponseTransformer = (model: Any) => gson.toJson(model)

		JakonInit.javalin.before("*", new Handler {
			override def handle(ctx: Context): Unit = {
				// also prepares page context
				if (!ctx.path().startsWith("/jakon/")) {
					Logger.debug("Processing req: " + ctx.path())
				}
			}
		})
		JakonInit.javalin.before(AdminPrefix, new Handler {
			override def handle(ctx: Context): Unit = {
				val user: JakonUser =  ctx.sessionAttribute("user") // request.session.attribute("user")
				if ((Settings.getDeployMode ne DeployMode.DEVEL)
				  && user != null
				  && (user.acl.adminAllowed || user.acl.masterAdmin)) {
					ctx.redirect(s"$AdminPrefix/index", HttpStatus.FOUND)
				}
			}
		})

		JakonInit.javalin.before(s"$AdminPrefix/*", new Handler {
			override def handle(ctx: Context): Unit = {
				if (ctx.path() != s"$AdminPrefix/register"
				  && ctx.path() != s"$AdminPrefix/logout"
				  && ctx.path() != s"$AdminPrefix/login"
				  && ctx.path() != s"$AdminPrefix/resetPassword"
				  && !ctx.path().startsWith(s"$AdminPrefix/login/oauth")) {
					var user: JakonUser = ctx.sessionAttribute("user")
					if ((Settings.getDeployMode eq DeployMode.DEVEL) && user == null) {
						DBHelper.withDbConnection(implicit conn => {
							user = UserService.getMasterAdmin()
							ctx.sessionAttribute("user", user)
						})
					}
					if (user == null || !user.acl.adminAllowed && !user.acl.masterAdmin) {
						ctx.redirect(AdminPrefix, HttpStatus.FOUND)
					}
				}
			}
		})


		JakonInit.javalin.get(AdminPrefix, new Handler {
			override def handle(ctx: Context): Unit = {
				val res = render(ctx, Authentication.loginGet(ctx))
				ctx.result(res)
			}
		})
		JakonInit.javalin.post(AdminPrefix, new Handler {
			override def handle(ctx: Context): Unit = {
				val res = render(ctx, Authentication.loginPost(ctx))
				ctx.result(res)
			}
		})

		ApiBuilder.path(s"$AdminPrefix", () => {
			if (Settings.getDeployMode == DeployMode.PRODUCTION) {
				JakonInit.javalin.exception(classOf[Exception], new AdminExceptionHandler)
			}

			get("/index", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, AdminSettings.dashboardController.apply(ctx))
					ctx.result(res)
				}
			})
			get("/logout", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, Authentication.logoutPost(ctx))
					ctx.result(res)
				}
			})
			get("/profile", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, UserController.render(ctx))
					ctx.result(res)
				}
			})
			post("/profile", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, UserController.update(ctx))
					ctx.result(res)
				}
			})
			get("/object/{name}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.getList(ctx))
					ctx.result(res)
				}
			})
			get("/object/create/{name}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.getItem(ctx))
					ctx.result(res)
				}
			})
			post("/object/create/{name}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.updateItem(ctx))
					ctx.result(res)
				}
			})
			get("/object/delete/{name}/{id}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.deleteItem(ctx))
					ctx.result(res)
				}
			})
			get("/object/moveUp/{name}/{id}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.moveInList(ctx, up = true))
					ctx.result(res)
				}
			})
			get("/object/moveDown/{name}/{id}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.moveInList(ctx, up = false))
					ctx.result(res)
				}
			})
			get("/object/{name}/{id}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.getItem(ctx))
					ctx.result(res)
				}
			})
			post("/object/{name}/{id}", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = render(ctx, ObjectController.updateItem(ctx))
					ctx.result(res)
				}
			})
		})

		if (AdminSettings.enableFiles) {
			path(s"$AdminPrefix/files", () => {
				get("/", new Handler {
					override def handle(ctx: Context): Unit = {
						val res = render(ctx, FileManagerController.getManager(ctx))
						ctx.result(res)
					}
				})
				get("/frame", new Handler {
					override def handle(ctx: Context): Unit = {
						val res = render(ctx, FileManagerController.getManagerFrame(ctx))
						ctx.result(res)
					}
				})
				get("/{method}", new Handler {
					override def handle(ctx: Context): Unit = {
						FileManagerController.executeGet(ctx)
					}
				})
				post("/{method}", new Handler {
					override def handle(ctx: Context): Unit = {
						FileManagerController.executePost(ctx)
					}
				})
			})
		}

		ApiBuilder.path(s"$AdminPrefix/api", () => {
			post("/search", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = gson.toJson(Api.search(ctx))
					ctx.result(res)
				}
			})
			post("/files", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = gson.toJson(Api.getFiles(ctx, Option.empty))
					ctx.result(res)
				}
			})
			post("/images", new Handler {
				override def handle(ctx: Context): Unit = {
					val res = gson.toJson(Api.getImages(ctx))
					ctx.result(res)
				}
			})
		})
		AdminSettings.customControllers.foreach((c: Class[_ <: AbstractController]) => {
			try {
				val instance = c.getDeclaredConstructor().newInstance()
				get(s"$AdminPrefix/" + instance.path(), new Handler {
					override def handle(ctx: Context): Unit = {
						val res = render(ctx, instance.doRender(ctx))
						ctx.result(res)
					}
				})
				val methods = c.getDeclaredMethods
				for (m <- methods) {
					val an = m.getAnnotation(classOf[ExecuteFun])
					if (an != null) {
						if (!m.isAccessible) m.setAccessible(true)
						an.method match {
							case x if x == HttpMethod.get =>
								get(s"$AdminPrefix/" + an.path, new Handler {
									override def handle(ctx: Context): Unit = {
										val res = render(ctx, m.invoke(instance, ctx).asInstanceOf[cz.kamenitxan.jakon.webui.Context])
										ctx.result(res)
									}
								})
							case HttpMethod.post =>
								post(s"$AdminPrefix/" + an.path, new Handler {
									override def handle(ctx: Context): Unit = {
										val res = render(ctx,  m.invoke(instance, ctx).asInstanceOf[cz.kamenitxan.jakon.webui.Context])
										ctx.result(res)
									}
								})
							case default => throw new UnsupportedOperationException(default.toString + " is not supported")
						}
					}
				}
				Logger.info("Custom admin controller registered: " + instance.getClass.getSimpleName)
			} catch {
				case e@(_: InstantiationException | _: IllegalAccessException) =>
					Logger.error("Failed to register custom controller", e)
			}
		})
		get(s"$AdminPrefix/*", new Handler {
			override def handle(ctx: Context): Unit = {
				Logger.warn("Unknown page requested - " + ctx.url())
				ctx.status(404)
				val res = render(ctx,new cz.kamenitxan.jakon.webui.Context(null, "errors/404"))
				ctx.result(res)
			}
		})
	}

}