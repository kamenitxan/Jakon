import {HeadingNode, QuoteNode, registerRichText} from "@lexical/rich-text";
import {createEditor} from "lexical";
import {mergeRegister} from "@lexical/utils";
import {createEmptyHistoryState, registerHistory} from "@lexical/history";
import {$convertFromMarkdownString, TRANSFORMERS} from "@lexical/markdown";

export default class JTextarea {

	init(fieldHash: string) {
		console.log("Initializing editor for field: " + fieldHash);

		const textarea: HTMLElement = document.getElementById('ta-' + fieldHash);
		const editorRef: HTMLElement = document.getElementById('lexical-editor' + fieldHash);
		const stateRef: HTMLElement = document.getElementById('lexical-state' + fieldHash);

		const initialConfig = {
			namespace: 'Vanilla JS Demo',
			// Register nodes specific for @lexical/rich-text
			nodes: [HeadingNode, QuoteNode],
			onError: (error: Error) => {
				throw error;
			},
			theme: {
				// Adding styling to Quote node, see styles.css
				quote: 'PlaygroundEditorTheme__quote',
			},
		};
		const editor = createEditor(initialConfig);
		editor.setRootElement(editorRef);

		// Registering Plugins
		/*mergeRegister(
			registerRichText(editor),
			registerHistory(editor, createEmptyHistoryState(), 300),
		);*/

		registerRichText(editor);
		registerHistory(editor, createEmptyHistoryState(), 300);

        editor.update(() => {
            $convertFromMarkdownString(textarea.textContent, TRANSFORMERS);
        });

		console.log("Done ts editor for field: " + fieldHash);
	}
}
