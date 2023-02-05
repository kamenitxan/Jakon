package core.functions

import cz.kamenitxan.jakon.core.template.function.FunctionHelper
import cz.kamenitxan.jakon.core.template.pebble.ValueFun
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite
import test.HelloFun

import java.util

@DoNotDiscover
class FunctionHelperTest extends AnyFunSuite {
	test("function split params") {
		val params = FunctionHelper.splitParams("param1=val1 param2=val2")
		assert(params.containsKey("param1"))
		assert(params.get("param2") == "val2")
	}

	test("function parse") {
		FunctionHelper.register(new HelloFun)
		val res = FunctionHelper.parse("{HelloFun()}")
		assert(res == "helloWorld")
	}

	test("value fun") {
		val fun = new ValueFun
		val first = null
		val second = null
		val third = "3"

		val args = new util.HashMap[String, AnyRef]()
		args.put(null, first)
		args.put(null, second)
		args.put(null, third)
		assert("3" == fun.execute(args, null, null, 0))
	}

	test("value fun - only null") {
		val fun = new ValueFun
		val first = null
		val second = null
		val third = null

		val args = new util.HashMap[String, AnyRef]()
		args.put(null, first)
		args.put(null, second)
		args.put(null, third)
		assert(null == fun.execute(args, null, null, 0))
	}

}