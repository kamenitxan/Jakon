package jakon

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
		val fileContents = Source.fromFile("out/page/test_page_1.html").getLines.mkString
		assert(fileContents.contains("url=\"/page/test_page_1\""))
		assert(fileContents.contains("title=\"test page 1\""))
		assert(fileContents.contains("content=\"test content\""))
		assert(fileContents.contains("published=\"true\""))
		assert(fileContents.contains("showComments=\"false\""))
	}
}
