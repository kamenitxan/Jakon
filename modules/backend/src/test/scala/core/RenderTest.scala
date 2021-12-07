package core

import org.scalatest.DoNotDiscover
import test.TestBase

import scala.io.Source

/**
  * Created by TPa on 27.08.16.
  */
@DoNotDiscover
class RenderTest extends TestBase {

	test("render static test") { _ =>
		val fileContents = Source.fromFile("out/static.html").getLines.mkString
		assert(fileContents.contains("__content__"))
	}

	test("render page") { _ =>
		val fileContents = Source.fromFile("out/page/test_page_1.html").getLines.mkString
		assert(fileContents.contains("url=\"/page/test_page_1\""))
		assert(fileContents.contains("title=\"test page 1\""))
		assert(fileContents.contains("content=\"test content\""))
		assert(fileContents.contains("published=\"true\""))
		assert(fileContents.contains("showComments=\"false\""))
	}

	test("render page with debug rerender") { f =>
		val url = host + "/page/test_page_1.html"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("url=\"/page/test_page_1\""))
		assert(f.driver.getPageSource.contains("title=\"test page 1\""))
		assert(f.driver.getPageSource.contains("content=\"test content\""))
		assert(f.driver.getPageSource.contains("published=\"true\""))
		assert(f.driver.getPageSource.contains("showComments=\"false\""))
	}
}
