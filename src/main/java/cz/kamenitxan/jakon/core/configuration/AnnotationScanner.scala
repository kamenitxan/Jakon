package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.controler.{Controller, Get, Post}
import cz.kamenitxan.jakon.core.dynamic.Pagelet
import cz.kamenitxan.jakon.core.template.TemplateEngine
import io.github.classgraph.{ClassGraph, ClassInfoList, ScanResult}
import spark.{Route, Spark, TemplateViewRoute}
import spark.Spark._


object AnnotationScanner {

	private val te: TemplateEngine = Settings.getTemplateEngine

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
		val controllers = scanResult.getClassesWithAnnotation(classOf[Controller].getCanonicalName).loadClasses()
		controllers.forEach(c => {
			val controllerAnn = c.getAnnotation(classOf[Controller])
			c.getDeclaredMethods
			  .filter(m => m.getAnnotation(classOf[Get]) != null || m.getAnnotation(classOf[Post]) != null)
			  .foreach(m => {
				  val get = m.getAnnotation(classOf[Get])
				  val post = m.getAnnotation(classOf[Post])
				  if (get != null && post != null) {
					  // TODO log
					  return
				  }
				  if (get != null) {
					   //TODO m.getReturnType.is
						Spark.get(controllerAnn.path() + get.path(), (req, res) => {
							val controller = c.newInstance().asInstanceOf[Pagelet]
							val context = m.invoke(controller, req, res).asInstanceOf[Map[String, AnyRef]]
							controller.render(context, get.template())
						})
				  }
				  if (post != null) {

				  }
			  })
		})
	}

}
