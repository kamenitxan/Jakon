package cz.kamenitxan.jakon.core.deploy

import java.time.LocalDateTime

class Server(
              val destination: String,
              var lastDeploy: LocalDateTime
            ) {
}
