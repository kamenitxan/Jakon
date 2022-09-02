package cz.kamenitxan.jakon

import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

/**
 * Created by TPa on 30.08.2022.
 */
object Circe {
	def main(args: Array[String]): Unit = {
		val rawJson: String =
			"""
				| {
				| "id": 42,
				| "name": "bar",
				| "inner": [{"name": "test"}, {"name": "test2"}],
				| "parent": {
				| 	"id": 42,
				|   "name": "bar"
				|  }
				| }
				|""".stripMargin

		println(rawJson)

		val res = parser.parse(rawJson).getOrElse(Json.Null)
		val f = res.hcursor.downField("id").top
		println(res)


	}


}

case class Inner(name: String)

case class Test(id: Int,
								name: String,
								inner: Seq[Inner]
							 )