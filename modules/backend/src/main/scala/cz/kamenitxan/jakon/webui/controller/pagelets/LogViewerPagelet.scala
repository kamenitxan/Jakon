package cz.kamenitxan.jakon.webui.controller.pagelets

import com.sun.management.HotSpotDiagnosticMXBean
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.logging.*
import io.javalin.http.Context
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.ByteArrayOutputStream

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


	@Get(path = "", template = "pagelet/logViewer")
	def render(ctx: Context): mutable.Map[String, Any] = {
		mutable.Map[String, Any](
			"logs" -> LogService.getLogs.reverse.asJava,
			"severities" -> Seq(Debug, Info, Warning, cz.kamenitxan.jakon.logging.Error, Critical).asJava
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
		
		fis.close()
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
