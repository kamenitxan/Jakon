package cz.kamenitxan.jakon.webui.api.objects

import cz.kamenitxan.jakon.core.model.JakonFile
import cz.kamenitxan.jakon.webui.api.AbstractResponse

import scala.jdk.CollectionConverters._


/**
  * Created by TPa on 29.04.18.
  */
class GetImagesRequest(val path: String)




class GetImagesResponse private(val result: java.util.List[JakonFile], success: Boolean) extends AbstractResponse(success) {

	def this(success: Boolean, files: Seq[JakonFile]) = {
		this(files.asJava, success)
	}

}

