package cz.kamenitxan.jakon.core.template.pebble

import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator

import java.lang.reflect.{AccessibleObject, Method}

/**
 * Created by TPa on 06.07.2023.
 */
class JakonMethodAccessValidator extends MethodAccessValidator {

	private val FORBIDDEN_METHODS = Seq("getClass", "wait", "notify", "notifyAll")

	override def isMethodAccessAllowed(obj: AnyRef, method: Method): Boolean = {
		val methodForbidden = obj.isInstanceOf[Runtime]
			|| obj.isInstanceOf[Thread]
			|| obj.isInstanceOf[ThreadGroup]
			|| obj.isInstanceOf[System]
		  || this.isUnsafeMethod(method)

			!methodForbidden
	}

	private def isUnsafeMethod(member: Method) = this.isAnyOfMethods(member, FORBIDDEN_METHODS)

	private def isAnyOfMethods(member: Method, methods: Seq[String]): Boolean = {
		methods.exists(method => this.isMethodWithName(member, method))
	}

	private def isMethodWithName(member: Method, method: String) = member.getName == method
}