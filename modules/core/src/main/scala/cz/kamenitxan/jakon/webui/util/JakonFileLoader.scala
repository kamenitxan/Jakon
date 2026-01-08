package cz.kamenitxan.jakon.webui.util

import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.FileLoader

import java.io.*


class JakonFileLoader(templateDir: String, loadFromJar: Boolean = false) extends FileLoader(templateDir) {
	val ADMIN_TMPL_DIR = "templates/admin"

	override def getReader(tmpl: String): Reader = {
		var isr: InputStreamReader = null
		var reader: Reader = null

		var is: InputStream = null
		is = loadFile(templateDir, tmpl)
		if (is == null && loadFromJar) {
			is = this.getClass.getResourceAsStream(s"/$ADMIN_TMPL_DIR/$tmpl.peb")
		}
		if (is == null) {
			throw new LoaderException(null, "Could not find template \"" + getPathBuilder(templateDir).toString() + tmpl + "\"")
		}

		try {
			isr = new InputStreamReader(is, getCharset)
			reader = new BufferedReader(isr)
		} catch {
			case e: UnsupportedEncodingException =>

		}
		reader
	}

	def loadFile(prefix: String, tmpl: String): FileInputStream = {
		// add the prefix and ensure the prefix ends with a separator character
		val path = getPathBuilder(prefix)

		var templateName = tmpl + (if (getSuffix == null) "" else getSuffix)

		/*
		 * if template name contains path segments, move those segments into the
		 * path variable. The below technique needs to know the difference
		 * between the path and file name.
		 */
		val pathSegments = templateName.split("\\\\|/")

		if (pathSegments.length > 1) { // file name is the last segment
			templateName = pathSegments(pathSegments.length - 1)
		}
		for (i <- 0 until pathSegments.length - 1) {
			path.append(pathSegments(i)).append(File.separatorChar)
		}

		// try to load File

		val file = new File(path.toString, templateName)
		if (file.exists && file.isFile) try
			return new FileInputStream(file)
		catch {
			case _: FileNotFoundException =>
		}
		null
	}

	def getPathBuilder(prefix: String): StringBuilder = {
		val sb = new StringBuilder("")
		if (prefix != null) {
			sb.append(prefix)
			if (!prefix.endsWith(String.valueOf(File.separatorChar))) sb.append(File.separatorChar)
		}
		sb
	}
}
