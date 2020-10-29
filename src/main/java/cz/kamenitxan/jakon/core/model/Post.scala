package cz.kamenitxan.jakon.core.model

import java.util.Date
import java.util.regex.Pattern

import cz.kamenitxan.jakon.core.function.FunctionHelper
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.ManyToOne

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
 */
class Post extends JakonObject {
	@JakonField(listOrder = 4)
	var date: Date = _
	@JakonField(required = false, listOrder = 2)
	var perex: String = _
	@ManyToOne
	@JakonField(required = false, inputTemplate = "String", listOrder = 5)
	var category: Category = _
	@JakonField(listOrder = 1, searched = true)
	var title: String = _
	@JakonField(inputTemplate = "textarea", listOrder = 3)
	var content: String = ""
	@JakonField(listOrder = 6)
	var showComments: Boolean = false


	def getContent: String = {
		if (content == null) return ""
		val parsedHtml = TemplateUtils.parseMarkdown(content)
		// TODO: parsovani funkci
		// (\{)((?:[a-z][a-z]+)).*?(\})
		val p = Pattern.compile("\\{((?:\\w+))\\((.*?)\\)}")
		val m = p.matcher(parsedHtml)
		val result = new StringBuffer
		while ( {
			m.find
		}) {
			val funcion = m.group(1)
			val params = m.group(2)
			m.appendReplacement(result, FunctionHelper.getFunction(funcion).execute(FunctionHelper.splitParams(params)))
		}
		m.appendTail(result)
		result.toString
	}

	override val objectSettings: ObjectSettings = null
}