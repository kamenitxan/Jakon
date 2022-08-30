package cz.kamenitxan.jakon.core.deploy

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.deploy.entity.Server
import cz.kamenitxan.jakon.core.model.KeyValueEntity
import cz.kamenitxan.jakon.core.service.KeyValueService
import cz.kamenitxan.jakon.logging.Logger

import java.lang.reflect.Type
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.jdk.CollectionConverters.*

object DeployDirector {
	private val KV_PREFIX = "SERVER_LAST_DEPLOY_"

	val servers: List[Server] = {
		implicit val conn: Connection = DBHelper.getConnection
		try {
			val gson = new Gson()
			import java.util
			val listType: Type = new TypeToken[util.ArrayList[Server]]() {}.getType
			// TODO: close source
			val s = gson.fromJson(Source.fromFile("servers.json").mkString, listType).asInstanceOf[util.ArrayList[Server]]
			val b = s.asScala.zipWithIndex
			  .map(zi => {
					val id = zi._2 + 1
					val s = zi._1
					val kve = KeyValueService.getByKey(KV_PREFIX + id)
					val lastDeployed = if (kve.nonEmpty) {
					  LocalDateTime.parse(kve.get.value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					} else {
					  null
					}
					new Server(id, s.url, s.path, lastDeployed)
				})
			b.toList
		} catch {
			case e: Throwable =>
				Logger.error("Failed to load deploy servers", e)
				List[Server]()
		} finally {
			conn.close()
		}


	}
	val deployer: IDeploy = {
		val cls = Class.forName(Settings.getDeployType)
		cls.newInstance().asInstanceOf[IDeploy]
	}

	def deploy(): Unit = {
		Director.render()
		servers.foreach(s => {
			deployer.deploy(s)
			s.lastDeployed = LocalDateTime.now()
			updateLastDeployTime(s)
		})
	}

	private def updateLastDeployTime(s: Server) = {
		val key = KV_PREFIX + s.id
		implicit val conn: Connection = DBHelper.getConnection
		try {
			KeyValueService.deleteByKey(key)
			val dateTime = s.lastDeployed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			val kve = new KeyValueEntity()
			kve.name = key
			kve.value = dateTime
			kve.create()
		} finally {
			conn.close()
		}
	}
}
