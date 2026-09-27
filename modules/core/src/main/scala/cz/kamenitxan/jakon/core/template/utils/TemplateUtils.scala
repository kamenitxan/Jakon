package cz.kamenitxan.jakon.core.template.utils

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.template.TemplateEngine
import cz.kamenitxan.jakon.logging.Logger
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import java.util.Objects
import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object TemplateUtils {

	def getEngine: TemplateEngine = Settings.getTemplateEngine

	private val suffixes = List(".xml", ".html", ".json", ".css", ".txt")
	private val parser = Parser.builder.build
	private val renderer = HtmlRenderer.builder.build

	val ReleaseInfix = "-release-"
	private val ReleaseFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")

	@volatile private var outputDirOverride: String = _

	private def outputDir: String = Option(outputDirOverride).getOrElse(Settings.getOutputDir)

	/**
	  * Redirects rendered pages to the given directory instead of Settings.getOutputDir while f is running.
	  */
	def withOutputDir[T](dir: String)(f: => T): T = {
		outputDirOverride = dir
		try f finally outputDirOverride = null
	}

	def saveRenderedPage(content: String, path: String): Unit = try {
		val file = new File(outputDir + "/" + path + getFileSuffix(path))
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

	/**
	  * Cleans the output dir and renders directly into it.
	  *
	  * @param outputDir output directory
	  * @param generate  renders the web into the directory passed as argument
	  */
	def renderInPlace(outputDir: String)(generate: String => Unit): Unit = {
		clean(outputDir)
		generate(outputDir)
	}

	/**
	  * Renders into a new release directory and then atomically switches the output dir symlink to it,
	  * so the web server always sees either the complete old or the complete new version.
	  * Falls back to renderInPlace when symlinks can't be created.
	  *
	  * @param outputDir output directory, becomes a symlink to the current release
	  * @param generate  renders the web into the directory passed as argument
	  */
	def renderAtomically(outputDir: String)(generate: String => Unit): Unit = {
		val link = Paths.get(outputDir).toAbsolutePath.normalize
		val parent = link.getParent
		val linkName = link.getFileName.toString
		val releasePrefix = linkName + ReleaseInfix
		val releaseName = releasePrefix + LocalDateTime.now.format(ReleaseFormat)
		val release = Iterator.from(0)
			.map(i => parent.resolve(if (i == 0) releaseName else s"$releaseName-$i"))
			.find(p => !Files.exists(p, LinkOption.NOFOLLOW_LINKS))
			.get
		val tmpLink = parent.resolve(linkName + ".tmp")

		Files.createDirectories(release)
		if (!createReleaseLink(tmpLink, release)) {
			deleteRecursively(release)
			renderInPlace(outputDir)(generate)
		} else {
			try {
				withOutputDir(release.toString) {
					generate(release.toString)
				}
			} catch {
				case e: Throwable =>
					Files.deleteIfExists(tmpLink)
					deleteRecursively(release)
					throw e
			}

			if (Files.isDirectory(link, LinkOption.NOFOLLOW_LINKS)) {
				// first run after switching to symlinks, real directory can't be atomically replaced
				Logger.warn(s"Output dir $link is a directory, replacing it with symlink")
				val legacy = parent.resolve(releasePrefix + "legacy")
				deleteRecursively(legacy)
				Files.move(link, legacy)
			}
			Files.move(tmpLink, link, StandardCopyOption.ATOMIC_MOVE)
			deleteOldReleases(parent, releasePrefix, release)
		}
	}

	private def createReleaseLink(tmpLink: Path, release: Path): Boolean = {
		try {
			Files.deleteIfExists(tmpLink)
			// relative target so the output dir can be moved or mounted elsewhere
			Files.createSymbolicLink(tmpLink, release.getFileName)
			true
		} catch {
			case e@(_: IOException | _: UnsupportedOperationException) =>
				Logger.warn("Could not create symlink for output dir, rendering in place", e)
				false
		}
	}

	private def deleteOldReleases(parent: Path, releasePrefix: String, current: Path): Unit = {
		try {
			val old = Using.resource(Files.list(parent)) { s =>
				s.iterator().asScala.filter(p => {
					p.getFileName.toString.startsWith(releasePrefix) && p != current && Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)
				}).toList
			}
			old.foreach(deleteRecursively)
		} catch {
			case e: IOException => Logger.error("Error occurred while deleting old releases", e)
		}
	}

	/**
	  * Deletes the given path including all its content. Symbolic links are deleted, not followed.
	  */
	@throws[IOException]
	def deleteRecursively(path: Path): Unit = if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
		Files.walkFileTree(path, new SimpleFileVisitor[Path] {
			override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
				Files.delete(file)
				FileVisitResult.CONTINUE
			}

			override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
				if (exc != null) throw exc
				Files.delete(dir)
				FileVisitResult.CONTINUE
			}
		})
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
