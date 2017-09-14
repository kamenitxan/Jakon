package cz.kamenitxan.jakon.core.deploy

import java.time.LocalDateTime
import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}

object DeployDirector {

	class CC[T] { def unapply(a:Any):Option[T] = Some(a.asInstanceOf[T]) }

	object M extends CC[Map[String, Any]]
	object L extends CC[List[Any]]
	object S extends CC[String]
	object D extends CC[Double]
	object B extends CC[Boolean]


	val servers: List[Server] = {
		/*for {
			Some(M(map)) <- List(scala.util.parsing.json.JSON.parseFull(jsonString))
			L(languages) = map("languages")
			M(language) <- languages
			S(name) = language("name")
			B(active) = language("is_active")
			D(completeness) = language("completeness")
		} yield {
			(name, active, completeness)
		}*/
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
			s.lastDeploy = LocalDateTime.now()
		})
	}
}
