package cz.kamenitxan.jakon.core.deploy.entity

import scala.beans.BeanProperty

class Server(
              @BeanProperty val url: String,
              @BeanProperty val path: String
            ) {
}
