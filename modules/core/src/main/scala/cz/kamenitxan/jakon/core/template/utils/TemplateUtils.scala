package cz.kamenitxan.jakon.core.template.utils

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.logging.Logger
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.nio.file.{FileVisitOption, Files, Path, Paths}
import java.util
import java.util.Objects

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object TemplateUtils {

	def getEngine: TemplateEngine = Settings.getTemplateEngine

	private val suffixes = List(".xml", ".html", ".json", ".css", ".txt")
	private val parser = Parser.builder.build
	private val renderer = HtmlRenderer.builder.build

	def saveRenderedPage(content: String, path: String): Unit = try {
		val file = new File(Settings.getOutputDir + "/" + path + getFileSuffix(path))
		// if file doesnt exists, then create it
		if (!file.exists) {
			file.getParentFile.mkdirs
			val created = file.createNewFile
			if (!created) throw new IOException("Could not create file.")
		}
		val fw = new FileWriter(file.getAbsoluteFile)
		val bw = new BufferedWriter(fw)
		bw.write(content)
		bw.close()
	} catch {
		case e: IOException => Logger.error("Error occurred while saving page", e)
	}

	def getFileSuffix(path: String): String = {
		val suffixSpecified = suffixes.exists(s => path.endsWith(s))
		if (suffixSpecified) "" else ".html"
	}

	/**
	  * Walks file tree starting at the given path and deletes all files
	  * but leaves the directory structure intact. If the given Path does not exist nothing
	  * is done.
	  */
	def clean(pathS: String): Unit = {
		val path = Paths.get(pathS)
		if (Files.exists(path)) try {
			validate(path)
			Files.walkFileTree(path, new CleanDirVisitor)
		} catch {
			case e: IOException => Logger.error("Error occurred while cleaning path", e)
		}
	}

	/**
	  * Copies a directory tree
	  *
	  * @param fromS source directory
	  * @param toS   target directory
	  */
	def copy(fromS: String, toS: String): Unit = {
		val from = Paths.get(fromS)
		val to = Paths.get(toS)
		try {
			validate(from)
			Files.walkFileTree(from, util.EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new CopyDirVisitor(from, to))
		} catch {
			case e: IOException => Logger.error("Error occurred while copying files", e)
		}
	}

	@throws[IOException]
	private def validate(paths: Path*): Unit = for (path <- paths) {
		Objects.requireNonNull(path)
		if (!Files.isDirectory(path)) {
			Files.createDirectories(path)
			if (!Files.isDirectory(path)) throw new IllegalArgumentException(String.format("%s is not a directory", path.toString))
		}
	}

	/**
	 * Parses given markdown text to html
	 * @param text markdown text
	 * @return html result
	 */
	def parseMarkdown(text: String): String = {
		val document = parser.parse(text)
		renderer.render(document)
	}
}
