package cz.kamenitxan.jakon.core.configuration

import java.util

import cz.kamenitxan.jakon.core.customPages.{CustomPage, StaticPage}
import cz.kamenitxan.jakon.core.dynamic.{Pagelet, PageletInitializer}
import io.github.classgraph.{ClassGraph, ScanResult}

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
		} finally {
			scanResult.close()
		}
	}

	private def loadControllers(scanResult: ScanResult): Unit = {
		val controllers: util.List[Class[_]] = scanResult.getClassesWithAnnotation(classOf[Pagelet].getCanonicalName).loadClasses()
		PageletInitializer.initControllers(controllers.asScala.toList)
	}

	private def loadCustomPages(scanResult: ScanResult): Unit = {
		val customPages = scanResult.getClassesWithAnnotation(classOf[CustomPage].getCanonicalName).loadClasses()
		val staticPages = scanResult.getClassesWithAnnotation(classOf[StaticPage].getCanonicalName).loadClasses()
		// TODO
	}

}
