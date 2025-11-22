package cz.kamenitxan.jakon.webui.api.objects

import cz.kamenitxan.jakon.core.model.JakonFile
import cz.kamenitxan.jakon.webui.api.AbstractResponse

import scala.jdk.CollectionConverters.*


/**
  * Created by TPa on 29.04.18.
  */
class GetFilesRequest(val path: String)


class GetFilesResponse private(val result: java.util.List[JakonFile], success: Boolean) extends AbstractResponse(success) {

	def this(success: Boolean, files: Seq[JakonFile]) = {
		this(files.asJava, success)
	}

}

