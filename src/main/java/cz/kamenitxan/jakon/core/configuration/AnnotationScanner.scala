package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.custom_pages.{CustomPage, CustomPageInitializer, StaticPage}
import cz.kamenitxan.jakon.core.dynamic.{Pagelet, PageletInitializer}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.controller.objectextension.{ObjectExtension, ObjectExtensionInitializer}
import io.github.classgraph.{ClassGraph, ClassInfoList, ScanResult}

import scala.collection.JavaConverters._


object AnnotationScanner {

	private val scanResult = {
		val cg = new ClassGraph().enableAllInfo()
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
			loadControllers(scanResult)
			loadCustomPages(scanResult)
			loadObjectExtensions(scanResult)
		} finally {
			scanResult.close()
		}
	}

	private def loadControllers(scanResult: ScanResult): Unit = {
		val controllers = scanResult.getClassesWithAnnotation(classOf[Pagelet].getCanonicalName).loadScalaClasses()
		PageletInitializer.initControllers(controllers)
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
			cil.loadClasses().asScala
		}
	}

}
