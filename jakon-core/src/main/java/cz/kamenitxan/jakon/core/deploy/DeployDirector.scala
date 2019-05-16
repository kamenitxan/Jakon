package cz.kamenitxan.jakon.core.deploy

import java.lang.reflect.Type
import java.time.LocalDateTime

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.deploy.entity.Server

import scala.collection.JavaConverters._
import scala.io.Source

object DeployDirector {

	val servers: List[Server] = {
		try {
			val gson = new Gson()
			import java.util
			val listType: Type = new TypeToken[util.ArrayList[Server]]() {}.getType
			val s = gson.fromJson(Source.fromFile("servers.json").mkString, listType).asInstanceOf[util.ArrayList[Server]]
			val b = s.asScala.zipWithIndex
			  .map(zi => {
				  val s = zi._1
				  new Server(zi._2 + 1, s.url, s.path, null)
			  })
			b.toList
		} catch {
			case e: Throwable =>
				e.printStackTrace()
				null
		}


	}
	val deployer: IDeploy = {
		val cls = Class.forName(Settings.getDeployType)
		cls.newInstance().asInstanceOf[IDeploy]
	}

	def init() = {

	}

	def deploy() = {
		Director.render()
		servers.foreach( s => {
			deployer.deploy(s)
			s.lastDeployed = LocalDateTime.now()
		})
	}
}
