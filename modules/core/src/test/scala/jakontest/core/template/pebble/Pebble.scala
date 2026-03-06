package jakontest.core.template.pebble

import cz.kamenitxan.jakon.core.template.pebble.MarkdownFilter
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

@DoNotDiscover
class Pebble extends AnyFunSuite {

	private val testContent = "```\nclass ExampleTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[ExampleTask].getSimpleName, period, unit) {  " +
	  "\n\toverride def start(): Unit = {\n\t\t???\n\t}\n}\n```\n\nThen in JakonInit\n\n```\ndef taskSetup(): Unit = {\n\t\tTaskRunner.registerTask(new ExampleTask(10, TimeUnit.MINUTES))\n}\n```\n"
	private val expectedContent = "<pre><code>class ExampleTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[ExampleTask].getSimpleName, period, unit) {  \n\toverride def start(): Unit = {\n\t\t???\n\t}\n}\n</code></pre>\n<p>Then in JakonInit</p>\n<pre><code>def taskSetup(): Unit = {\n\t\tTaskRunner.registerTask(new ExampleTask(10, TimeUnit.MINUTES))\n}\n</code></pre>\n"

	test("markdownFilter") {
		val res = MarkdownFilter.parseString(testContent)
		assert(res == expectedContent)
	}
}
