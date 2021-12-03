package core.template.pebble

import cz.kamenitxan.jakon.core.template.pebble.MarkdownFilter
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

@DoNotDiscover
class Pebble extends AnyFunSuite {

	val testContent = "```\nclass ExampleTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[ExampleTask].getSimpleName, period, unit) {  " +
	  "\n\toverride def start(): Unit = {\n\t\t???\n\t}\n}\n```\n\nThen in JakonInit\n\n```\ndef taskSetup(): Unit = {\n\t\tTaskRunner.registerTask(new ExampleTask(10, TimeUnit.MINUTES))\n}\n```\n"
	val expectedContent = "<pre><code>class ExampleTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[ExampleTask].getSimpleName, period, unit) {  <br>&nbsp;&nbsp;&nbsp;&nbsp;override def start(): Unit = {<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;???<br>&nbsp;&nbsp;&nbsp;&nbsp;}<br>}<br></code></pre><br><p>Then in JakonInit</p><br><pre><code>def taskSetup(): Unit = {<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TaskRunner.registerTask(new ExampleTask(10, TimeUnit.MINUTES))<br>}<br></code></pre><br>"

	test("markdownFilter") {
		val res = MarkdownFilter.parseString(testContent)
		assert(res == expectedContent)
	}
}
