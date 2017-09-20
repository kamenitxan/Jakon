package cz.kamenitxan.jakon.core.deploy.entity

import java.time.LocalDateTime

import scala.beans.BeanProperty

class Server(
              @BeanProperty val url: String,
              @BeanProperty val path: String
            ) {
}
