package cz.kamenitxan.jakon.core.deploy

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.deploy.entity.Server
import net.liftweb.json.{DefaultFormats, JsonParser}

import scala.io.Source

object DeployDirector {

	val servers: List[Server] = {
		implicit val formats = DefaultFormats
		//TODO: cacht exception when file not found
		//TODO: rewrite to gson
		val json = JsonParser.parse(Source.fromFile("servers.json").mkString)
		json.extract[List[Server]]
	}
	val deployer: IDeploy = {
		val cls = Class.forName(Settings.getProperty(SettingValue.DEPLOY_TYPE))
		cls.newInstance().asInstanceOf[IDeploy]
	}

	def init() = {

	}

	def deploy() = {
		Director.render()
		servers.foreach( s => {
			deployer.deploy(s)
			//s.lastDeploy = LocalDateTime.now()
		})
	}
}
