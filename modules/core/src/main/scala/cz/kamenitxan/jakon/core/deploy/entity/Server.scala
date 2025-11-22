package cz.kamenitxan.jakon.core.deploy.entity

import java.time.LocalDateTime

class Server(
							val id: Int,
							val url: String,
							val path: String,
							var lastDeployed: LocalDateTime
							) {


	override def toString = s"Server(id=$id, url=$url, path=$path, lastDeployed=$lastDeployed)"
}
