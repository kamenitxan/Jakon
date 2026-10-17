package jakontest.core.template.pebble

import cz.kamenitxan.jakon.core.template.pebble.MarkdownFilter
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source
import scala.util.Using

/**
  * Checks that markdown produced by jakon editor (sample from Jakon-editor/src/index.html) is rendered to html.
  */
@DoNotDiscover
class MarkdownEditorSampleTest extends AnyFunSuite {

	private lazy val html = {
		val sample = Using.resource(Source.fromResource("markdown/editor-sample.md", getClass.getClassLoader))(_.mkString)
		MarkdownFilter.parseString(sample)
	}

	private def assertRendered(fragment: String): Unit = {
		// multiline literals contain \r\n when sources are checked out with CRLF (Windows)
		val expected = fragment.replace("\r\n", "\n")
		assert(html.contains(expected), s"\nexpected fragment:\n$expected\nrendered html:\n$html")
	}

	test("editor sample headings") {
		assertRendered("<h1>title h1</h1>")
		assertRendered("<h2>title h2</h2>")
		assertRendered("<h3>title h3</h3>")
	}

	test("editor sample link") {
		assertRendered("""<p>Hello, <a href="https://playground.lexical.dev/">Lexical</a></p>""")
	}

	test("editor sample horizontal rule") {
		assertRendered("<hr />")
	}

	test("editor sample inline formatting") {
		assertRendered("<em>italic</em>")
		assertRendered("<del>strikethrough</del>")
		assertRendered("<strong>bold</strong>")
		assertRendered("<em><strong>bold italic</strong></em>")
		assertRendered("<code>inline code</code>")
	}

	// autolinks are not supported yet (needs commonmark-ext-autolink)
	ignore("editor sample auto links") {
		assertRendered("""<a href="https://example.com">https://example.com</a>""")
		assertRendered("""<a href="mailto:me@example.com">me@example.com</a>""")
	}

	test("editor sample quote") {
		assertRendered("<blockquote>\n<p>A quote</p>\n<p>with two paragraphs</p>\n</blockquote>")
	}

	test("editor sample bullet list") {
		assertRendered("<ul>\n<li>bullet one</li>\n<li>bullet two\n<ul>\n<li>nested bullet</li>\n</ul>\n</li>\n</ul>")
	}

	test("editor sample ordered list") {
		assertRendered("<ol>\n<li>first</li>\n<li>second</li>\n</ol>")
	}

	test("editor sample task list") {
		assert(!html.contains("[ ]") && !html.contains("[x]"), s"\ntask list markers were not rendered:\n$html")
		assertRendered("""type="checkbox"""")
		assertRendered("checked")
	}

	test("editor sample code block") {
		assertRendered(
			"""<pre><code class="language-scala">case class Point(x: Int, y: Int) {
				|  def move(dx: Int) = copy(x = x + dx)
				|}
				|</code></pre>""".stripMargin)
	}

	test("editor sample table") {
		assertRendered("<table>")
		assertRendered("<th>unknown syntax</th>")
		assertRendered("<td>1</td>")
	}

	test("editor sample image") {
		assertRendered("""<img src="http://localhost:4566/upload/testFolder2/photomode_29122023_191904.png" alt="photomode_29122023_191904.png" />""")
	}
}
