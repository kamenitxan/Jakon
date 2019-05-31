package cz.kamenitxan.jakon.core.model.Dao

import java.lang.reflect.{InvocationHandler, Method}

/**
  * Created by TPa on 2019-03-12.
  */
class IncrementingStatement(val target: AnyRef) extends InvocationHandler {


	override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
		method.invoke(target, args: _*)
	}
}
