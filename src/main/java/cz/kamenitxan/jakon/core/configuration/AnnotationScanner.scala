package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.customPages.{CustomPage, CustomPageInitializer, StaticPage}
import cz.kamenitxan.jakon.core.dynamic.{Pagelet, PageletInitializer}
import io.github.classgraph.{ClassGraph, ClassInfoList, ScanResult}

import scala.collection.JavaConverters._


object AnnotationScanner {

	def scan(): Unit = {
		val scanResult: ScanResult = new ClassGraph()
		  .enableAllInfo()
  		//.verbose()
		  .whitelistPackages("cz.kamenitxan.jakon", Settings.getPackage)
		  .scan()
		try {
			// Use scanResult here
			loadControllers(scanResult)
			loadCustomPages(scanResult)
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

	implicit class ClassInfoListExtensions(val cil: ClassInfoList) {

		def loadScalaClasses(): Seq[Class[_]] = {
			cil.loadClasses().asScala
		}
	}
}
