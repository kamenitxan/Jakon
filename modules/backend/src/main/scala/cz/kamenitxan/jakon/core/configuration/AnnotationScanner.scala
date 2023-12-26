package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.custom_pages.{CustomPage, CustomPageInitializer, StaticPage}
import cz.kamenitxan.jakon.core.dynamic.{JsonPagelet, JsonPageletInitializer, Pagelet, PageletInitializer}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.controller.objectextension.{ObjectExtension, ObjectExtensionInitializer}
import io.github.classgraph.{ClassGraph, ClassInfoList, ScanResult}

import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption}
import java.util.regex.Pattern
import scala.jdk.CollectionConverters._


class AnnotationScanner {

	private val scanResult = {
		val cg = new ClassGraph().enableAllInfo()
		cg.whitelistPaths("/static")
		Settings.getPackage.foreach(p => cg.whitelistPackages(p))

		val result = Utils.measured(elapsedTime => "Annotations scanned in " + elapsedTime + " ms") {
			cg.scan()
		}
		result
	}


	def loadConfiguration(): Unit = {
		loadConfiguration(scanResult)
	}

	def load(): Unit = {
		try {
			if (Settings.isInitRoutes) {
				loadControllers(scanResult)
				loadObjectExtensions(scanResult)
			}
			loadCustomPages(scanResult)
			copyResources()
		} finally {
			scanResult.close()
		}
	}

	/** copy static resources to static folder, so they can be server by nginx */
	private def copyResources(): Unit = {
		val resourceList = scanResult.getResourcesMatchingPattern(Pattern.compile(".*static.*"))
		resourceList.forEach(r => {
			val targetFile = new File(Settings.getStaticDir + "/" + r.getPath.replace("static/", ""))
			targetFile.getParentFile.mkdirs()
			targetFile.exists()
			Files.copy(r.open(), targetFile.toPath, StandardCopyOption.REPLACE_EXISTING)
		})
	}

	private def loadControllers(scanResult: ScanResult): Unit = {
		val controllers = scanResult.getClassesWithAnnotation(classOf[Pagelet].getCanonicalName).loadScalaClasses()
		val jsonControllers = scanResult.getClassesWithAnnotation(classOf[JsonPagelet].getCanonicalName).loadScalaClasses()
		PageletInitializer.initControllers(controllers)
		JsonPageletInitializer.initControllers(jsonControllers)
	}

	private def loadCustomPages(scanResult: ScanResult): Unit = {
		val customPages = scanResult.getClassesWithAnnotation(classOf[CustomPage].getCanonicalName).loadScalaClasses()
		val staticPages = scanResult.getClassesWithAnnotation(classOf[StaticPage].getCanonicalName).loadScalaClasses()
		CustomPageInitializer.initCustomPages(customPages)
		CustomPageInitializer.initStaticPages(staticPages)
	}

	private def loadConfiguration(scanResult: ScanResult): Unit = {
		val config = scanResult.getClassesWithAnnotation(classOf[Configuration].getCanonicalName).loadScalaClasses()
		ConfigurationInitializer.initConfiguration(config)
	}

	private def loadObjectExtensions(scanResult: ScanResult): Unit = {
		val config = scanResult.getClassesWithAnnotation(classOf[ObjectExtension].getCanonicalName).loadScalaClasses()
		ObjectExtensionInitializer.initObjectExtensions(config)
	}

	implicit class ClassInfoListExtensions(val cil: ClassInfoList) {

		def loadScalaClasses(): Seq[Class[_]] = {
			cil.loadClasses().asScala.toSeq
		}
	}

}
