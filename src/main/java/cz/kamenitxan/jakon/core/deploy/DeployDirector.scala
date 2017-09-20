package cz.kamenitxan.jakon.core.deploy

import java.io.File
import java.time.LocalDateTime

import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.deploy.entity.Server
import net.liftweb.json.{DefaultFormats, JsonParser}

import scala.io.Source

object DeployDirector {

	val servers: List[Server] = {
		implicit val formats = DefaultFormats
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
		servers.foreach( s => {
			deployer.deploy(s)
			//s.lastDeploy = LocalDateTime.now()
		})
	}
}
