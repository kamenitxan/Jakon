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
}
