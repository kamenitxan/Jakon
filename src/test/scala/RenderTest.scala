import org.scalatest.FunSuite

import scala.io.Source

/**
  * Created by TPa on 27.08.16.
  */
class RenderTest extends FunSuite {

	test("render static test") {
		val fileContents = Source.fromFile("out/static.html").getLines.mkString
		assert(fileContents.contains("__content__"))
	}

	test("render page") {
		val fileContents = xml.XML.loadString(Source.fromFile("out/page/testpage1.html").getLines.mkString)
		assert(fileContents.attribute("id").mkString === "1")
		assert(fileContents.attribute("url").mkString === "/page/testpage1")
		assert(fileContents.attribute("sectionName").mkString === "")
		assert(fileContents.attribute("published").mkString === "true")
		assert(fileContents.attribute("title").mkString === "test page 1")
		assert(fileContents.attribute("content").mkString === "test content")
		assert(fileContents.attribute("showComments").mkString === "false")

	}
}
