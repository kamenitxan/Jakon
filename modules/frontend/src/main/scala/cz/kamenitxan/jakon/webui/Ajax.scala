package cz.kamenitxan.jakon.webui

import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLSelectElement, Headers, HttpMethod, RequestInit, RequestMode, RequestRedirect, Response, document}

import scala.scalajs.js
import scala.scalajs.js.{JSON, Promise}

object Ajax {

	import scala.concurrent.ExecutionContext.Implicits.global
	import js.Thenable.Implicits._

	def post(url: String, data: js.Any): Promise[String] = {
		dom.fetch(url, new RequestInit {
			body = js.JSON.stringify(data)
			headers = new Headers(
				js.Array(
					js.Array("Content-Type", "application/json")
				)
			)

			method = HttpMethod.POST
			mode = RequestMode.cors // no-cors, cors, *same-origin
			redirect =  RequestRedirect.follow  // manual, *follow, error
			//referrer=  'no-referrer' // *client, no-referrer
		}).`then`((response: Response) => {
			if (response.ok) {
				response.text()
			} else {
				println(s"Ajax request failed")
				Promise.reject(response.status.toString)
			}
		})
	}

}
