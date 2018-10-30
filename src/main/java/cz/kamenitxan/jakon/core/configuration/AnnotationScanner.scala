package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.controler.Controller
import io.github.classgraph.{ClassGraph, ClassInfoList, ScanResult}

object AnnotationScanner {

	def scan(): Unit = {
		val scanResult: ScanResult = new ClassGraph()
		  .enableAllInfo()
  		.verbose()
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
		val controllers: ClassInfoList = scanResult.getClassesWithAnnotation(classOf[Controller].getCanonicalName)
		controllers.
	}

}
