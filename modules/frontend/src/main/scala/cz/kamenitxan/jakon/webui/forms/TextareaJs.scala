package cz.kamenitxan.jakon.webui.forms

import org.scalajs.dom.{HTMLElement, document, window}
import typings.lexical.lexicalEditorMod.CreateEditorArgs
import typings.lexical.mod.LexicalEditor
import typings.lexicalRichText.mod.HeadingNode

import scala.scalajs.js

/**
	* Created by Kamenitxan on 18.09.2025
	*/
object TextareaJs {

	def init(fieldHash: Int): Unit = {
		println("Initializing editor for field: " + fieldHash)

		val editorRef = document.getElementById("lexical-editor" + fieldHash).asInstanceOf[HTMLElement]
		val stateRef = document.getElementById("lexical-state" + fieldHash).asInstanceOf[HTMLElement]

		val conf = CreateEditorArgs()
		conf.namespace = "editor" + fieldHash
		//conf.nodes = Seq(HeadingNode).toArray


		val editor = typings.lexical.mod.createEditor(conf).asInstanceOf[LexicalEditor]
		editor.setRootElement(editorRef)

		js.Dynamic.global.editor = editor
		js.Dynamic.global.rootElement = document.getElementById("editor-container" + fieldHash).asInstanceOf[HTMLElement]

		typings.lexicalUtils.mod.mergeRegister(() => {
			println("Registering rich text")
			typings.lexicalRichText.mod.registerRichText(editor)
		})

		println("Editor initialized for field: " + fieldHash)

	}

}
