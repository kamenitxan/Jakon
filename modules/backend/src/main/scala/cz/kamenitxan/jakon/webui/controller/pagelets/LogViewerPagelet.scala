package cz.kamenitxan.jakon.webui.controller.pagelets

import com.sun.management.HotSpotDiagnosticMXBean
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.logging.*
import io.javalin.http.Context

import java.io.{File, FileInputStream, IOException}
import java.lang.management.ManagementFactory
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
 * Created by TPa on 15.03.2022.
 */
@Pagelet(path = "/admin/logs", showInAdmin = true)
class LogViewerPagelet extends AbstractAdminPagelet {
	override val name: String = this.getClass.getSimpleName
	override val icon: String = "fa-exclamation-triangle"

	private val pageSize = 200

	@Get(path = "", template = "pagelet/logViewer")
	def render(ctx: Context): mutable.Map[String, Any] = {
		val severity = Option.apply(ctx.queryParam("severity"))
		val pageNumber = Option.apply(ctx.queryParam("page")).map(_.toInt).getOrElse(1)
		val from = (pageNumber - 1) * pageSize
		val to = from + pageSize
		val logs = LogService.getLogs.filter(l => {
			severity match {
				case Some(s) => l.severity.toString == s
				case None => true
			}
		}).reverse
		mutable.Map[String, Any](
			"logs" -> logs.slice(from, to).asJava,
			"pageNumber" -> pageNumber,
			"pageCount" -> Math.max(Math.ceil(logs.size / pageSize.toFloat), 1),
			"severities" -> Seq(Debug, Info, Warning, cz.kamenitxan.jakon.logging.Error, Critical).map(_.toString).asJava,
			"selectedSeverity" -> severity.getOrElse("ALL")
		)
	}

	@Get(path = "/heapdump", template = "pagelet/logViewer")
	def heapdump(ctx: Context): mutable.Map[String, Any] = {
		val file = new File("heapdump.hprof")
		if (file.exists()) {
			file.delete()
		}
		dumpHeap(file.getAbsolutePath, true)


		ctx.contentType("application/hprof")
		ctx.res().setContentLength(file.length().toInt)

		val fis = new FileInputStream(file)
		ctx.result(fis)
		
		file.delete()
		null
	}

	@throws[IOException]
	private def dumpHeap(filePath: String, live: Boolean): Unit = {
		val server = ManagementFactory.getPlatformMBeanServer
		val mxBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", classOf[HotSpotDiagnosticMXBean])
		mxBean.dumpHeap(filePath, live)
	}
}
