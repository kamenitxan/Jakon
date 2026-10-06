package cz.kamenitxan.jakon.core.dynamic

import cz.kamenitxan.jakon.core.configuration.ConfigurationInitializer
import cz.kamenitxan.jakon.logging.Logger

/**
 * Decides which pagelets get activated during startup.
 *
 * The state of a pagelet is resolved in this order (first match wins):
 *  1. configuration property `pagelet.SimpleName` (e.g. `pagelet.DbConsolePagelet=DISABLED`)
 *  2. [[ConditionalPagelet.defaultState]] on the pagelet companion object
 *  3. [[PageletState.ENABLED]]
 *
 * Configuration has higher priority on purpose, so that the deployment can always override
 * a default coming from the code.
 */
object PageletSettings {

	private val ConfigPrefix = "pagelet."

	def stateOf(cls: Class[_]): PageletState = {
		configState(cls)
			.orElse(conditionalState(cls))
			.getOrElse(PageletState.ENABLED)
	}

	/** @return true if the routes of the pagelet should be registered */
	def isEnabled(cls: Class[_]): Boolean = stateOf(cls) != PageletState.DISABLED

	private def configState(cls: Class[_]): Option[PageletState] = {
		ConfigurationInitializer.getConf.get(ConfigPrefix + cls.getSimpleName).flatMap(value => {
			try {
				Option(PageletState.valueOf(value.trim.toUpperCase))
			} catch {
				case _: IllegalArgumentException =>
					Logger.error(s"Unknown pagelet state '$value' for ${cls.getSimpleName}. Expected one of " +
						PageletState.values().map(_.name()).mkString(", ") + ". Ignoring.")
					Option.empty
			}
		})
	}

	/**
	 * Asks the companion object of the pagelet whether it wants to be active.
	 */
	private def conditionalState(cls: Class[_]): Option[PageletState] = {
		companionOf(cls).flatMap(companion => {
			try {
				Option(companion.defaultState)
			} catch {
				case ex: Exception =>
					Logger.error(s"Could not resolve default state of ${cls.getSimpleName}", ex)
					Option.empty
			}
		})
	}

	/** @return the companion object of the pagelet if it implements [[ConditionalPagelet]] */
	private def companionOf(cls: Class[_]): Option[ConditionalPagelet] = {
		try {
			val companionCls = Class.forName(cls.getName + "$", true, cls.getClassLoader)
			companionCls.getField("MODULE$").get(null) match {
				case cp: ConditionalPagelet => Option(cp)
				case _ => Option.empty
			}
		} catch {
			case _: ClassNotFoundException => Option.empty
			case _: NoSuchFieldException => Option.empty
			case ex: Exception =>
				Logger.error(s"Could not load companion object of ${cls.getSimpleName}", ex)
				Option.empty
		}
	}
}
