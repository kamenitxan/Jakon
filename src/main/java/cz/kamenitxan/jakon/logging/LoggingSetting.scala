package cz.kamenitxan.jakon.logging

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}

/**
  * Created by TPa on 16/11/2019.
  */
@Configuration
object LoggingSetting {

	@ConfigurationValue(name = "LOGGING.maxlimg", required = true, defaultValue = "100000")
	private var maxLimit: Int = _
	@ConfigurationValue(name = "LOGGING.softLimit", required = true, defaultValue = "50000")
	private var softLimit: Int = _
	@ConfigurationValue(name = "LOGGING.maxCriticalAge", required = true, defaultValue = "43200")
	private var maxCriticalAge: Int = _
	@ConfigurationValue(name = "LOGGING.maxErrorAge", required = true, defaultValue = "14400")
	private var maxErrorAge: Int = _
	@ConfigurationValue(name = "LOGGING.maxWarningAge", required = true, defaultValue = "7200")
	private var maxWarningAge: Int = _
	@ConfigurationValue(name = "LOGGING.maxInfoAge", required = true, defaultValue = "1440")
	private var maxInfoAge: Int = _
	@ConfigurationValue(name = "LOGGING.maxDebugAge", required = true, defaultValue = "60")
	private var maxDebugAge: Int = _

	def getMaxLimit: Int = maxLimit

	def setMaxLimit(maxLimit: String): Unit = this.maxLimit = maxLimit.toInt

	def getSoftLimit: Int = softLimit

	def setSoftLimit(softLimit: String): Unit = this.softLimit = softLimit.toInt

	def getMaxCriticalAge: Int = maxCriticalAge

	def setMaxCriticalAge(maxCriticalAge: String): Unit = this.maxCriticalAge = maxCriticalAge.toInt

	def getMaxErrorAge: Int = maxErrorAge

	def setMaxErrorAge(maxErrorAge: String): Unit = this.maxErrorAge = maxErrorAge.toInt

	def getMaxWarningAge: Int = maxWarningAge

	def setMaxWarningAge(maxWarningAge: String): Unit = this.maxWarningAge = maxWarningAge.toInt

	def getMaxInfoAge: Int = maxInfoAge

	def setMaxInfoAge(maxInfoAge: String): Unit = this.maxInfoAge = maxInfoAge.toInt

	def getMaxDebugAge: Int = maxDebugAge

	def setMaxDebugAge(maxDebugAge: String): Unit = this.maxDebugAge = maxDebugAge.toInt
}
