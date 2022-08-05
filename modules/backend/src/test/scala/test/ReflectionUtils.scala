package test

/**
  * Created by TPa on 08/04/2021.
  */
object ReflectionUtils {

	def changeFieldValue(fieldName: String, inst: AnyRef, value: Any): Unit = {
		val field = inst.getClass.getDeclaredField(fieldName)
		field.set(inst, value)
	}

	def changeObjectFieldValue(fieldName: String, objectName: String, value: Any): Unit = {
		???
		/*import scala.reflect.runtime.{universe => ru}
		val mirror = ru.runtimeMirror(getClass.getClassLoader)

		val moduleSymbol = mirror.staticModule(objectName)
		val moduleMirror = mirror.reflectModule(moduleSymbol)
		val instanceMirror = mirror.reflect(moduleMirror.instance)

		val field = moduleSymbol.typeSignature.declarations
		  .filter(d => d.asTerm.isVal || d.asTerm.isVar)
		  .filter(f => f.name.toString.strip() == fieldName)
		  .head
		val fieldMirror = instanceMirror.reflectField(field.asTerm)
		fieldMirror.set(value)*/

	}
}
