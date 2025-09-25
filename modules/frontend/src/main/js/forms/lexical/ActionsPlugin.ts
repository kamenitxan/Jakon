import {$createTextNode, $getRoot, LexicalEditor} from "lexical";
import {$createCodeNode, $isCodeNode} from '@lexical/code';
import {
    $convertFromMarkdownString,
    $convertToMarkdownString,
} from '@lexical/markdown';
import {MARKDOWN_TRANSFORMERS} from "./MarkdownTransforme";

export class Toolbar {

    shouldPreserveNewLinesInMarkdown = true;
    editor: LexicalEditor

    init(editor: LexicalEditor, root: HTMLElement) {
        this.editor = editor;
    }

    handleMarkdownToggle()  {
        this.editor.update(() => {
            const root = $getRoot();
            const firstChild = root.getFirstChild();
            if ($isCodeNode(firstChild) && firstChild.getLanguage() === 'markdown') {
                $convertFromMarkdownString(
                    firstChild.getTextContent(),
                    MARKDOWN_TRANSFORMERS,
                    undefined, // node
                    this.shouldPreserveNewLinesInMarkdown,
                );
            } else {
                const markdown = $convertToMarkdownString(
                    MARKDOWN_TRANSFORMERS,
                    undefined, //node
                    this.shouldPreserveNewLinesInMarkdown,
                );
                const codeNode = $createCodeNode('markdown');
                codeNode.append($createTextNode(markdown));
                root.clear().append(codeNode);
                if (markdown.length === 0) {
                    codeNode.select();
                }
            }
        });
    }

}

