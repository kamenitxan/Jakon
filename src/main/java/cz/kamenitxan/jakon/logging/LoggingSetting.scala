package cz.kamenitxan.jakon.logging

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}

/**
  * Created by TPa on 16/11/2019.
  */
@Configuration
object LoggingSetting {

	@ConfigurationValue(name = "LOGGING.maxlimg", required = false, defaultValue = "100000")
	var maxLimit: Int = _
	@ConfigurationValue(name = "LOGGING.softLimit", required = false, defaultValue = "50000")
	var softLimit: Int = _
}
