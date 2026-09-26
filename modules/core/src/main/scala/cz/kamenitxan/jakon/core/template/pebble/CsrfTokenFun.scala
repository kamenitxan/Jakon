package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.security.CsrfProtection
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util

/**
  * @return CSRF token for current session or empty string outside of request
  */
class CsrfTokenFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val pc = PageContext.getInstance()
		if (pc == null || pc.ctx == null) "" else CsrfProtection.getOrCreateToken(pc.ctx)
	}

	override def getArgumentNames: util.List[String] = null
}
