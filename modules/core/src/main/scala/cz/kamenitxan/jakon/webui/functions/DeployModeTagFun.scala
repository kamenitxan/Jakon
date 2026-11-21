package cz.kamenitxan.jakon.webui.functions

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util

/**
  * @return "[PROD]" when running in PRODUCTION deploy mode, "[DEV]" otherwise (DEVEL/TESTING)
  */
class DeployModeTagFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		if (Settings.getDeployMode == DeployMode.PRODUCTION) "[PROD]" else "[DEV]"
	}

	override def getArgumentNames: util.List[String] = null
}
