package cz.kamenitxan.jakon.core.deploy.entity

import java.time.LocalDateTime

import scala.beans.BeanProperty

class Server(
              @BeanProperty val id: Int,
              @BeanProperty val url: String,
              @BeanProperty val path: String,
              @BeanProperty var lastDeployed: LocalDateTime
            ) {


	override def toString = s"Server(id=$id, url=$url, path=$path, lastDeployed=$lastDeployed)"
}
