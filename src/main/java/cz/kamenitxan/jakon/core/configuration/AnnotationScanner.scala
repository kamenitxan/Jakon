package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.template.TemplateEngine
import io.github.classgraph.{ClassGraph, ScanResult}
import spark.Spark
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._

import scala.collection.mutable


object AnnotationScanner {

	private val te: TemplateEngine = Settings.getTemplateEngine

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
		val controllers = scanResult.getClassesWithAnnotation(classOf[Pagelet].getCanonicalName).loadClasses()
		controllers.forEach(c => {
			val controllerAnn = c.getAnnotation(classOf[Pagelet])
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
							val controller = c.newInstance().asInstanceOf[AbstractPagelet]
							if (m.getParameterCount == 3){
								val dataType: Class[_] = m.getParameterTypes.drop(2).head
								val data = dataType.newInstance().asInstanceOf[AnyRef]
								dataType.getFields.foreach(f => {
									f.set(data, req.queryParams(f.getName).conform(f))
								})
								val context = m.invoke(controller, req, res, data).asInstanceOf[mutable.Map[String, AnyRef]]
								controller.render(context, get.template())
							} else {
								val context = m.invoke(controller, req, res).asInstanceOf[mutable.Map[String, AnyRef]]
								controller.render(context, get.template())
							}
						})
				  }
				  if (post != null) {
					  Spark.post(controllerAnn.path() + post.path(), (req, res) => {
						  val controller = c.newInstance().asInstanceOf[AbstractPagelet]
						  if (m.getParameterCount == 3){
							  val dataType: Class[_] = m.getParameterTypes.drop(2).head
							  val data = dataType.newInstance().asInstanceOf[AnyRef]
							  dataType.getFields.foreach(f => {
								  f.set(data, req.queryParams(f.getName).conform(f))
							  })
							  m.invoke(controller, req, res, data)
							  val context = new mutable.HashMap[String, AnyRef]()
							  controller.render(context, get.template())
						  } else {
							   m.invoke(controller, req, res).asInstanceOf[Map[String, AnyRef]]
							  val context = new mutable.HashMap[String, AnyRef]()
							  controller.render(context, get.template())
						  }
					  })
				  }
			  })
		})
	}

}
