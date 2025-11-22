package jakontest.test

/**
  * Created by TPa on 08/04/2021.
  */
object ReflectionUtils {

	def changeFieldValue(fieldName: String, inst: AnyRef, value: Any): Unit = {
		val field = inst.getClass.getDeclaredField(fieldName)
		field.setAccessible(true)
		field.set(inst, value)
	}

	def changeObjectFieldValue(fieldName: String, objectName: String, value: Any): Unit = {
		val cls = Class.forName(objectName+"$")
		val instance = cls.getDeclaredField("MODULE$").get(null)
		changeFieldValue(fieldName, instance, value)
	}
}
