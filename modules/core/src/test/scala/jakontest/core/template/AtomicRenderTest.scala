package jakontest.core.template

import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, LinkOption, Path}
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

@DoNotDiscover
class AtomicRenderTest extends AnyFunSuite {

	private def withTempDir(f: Path => Unit): Unit = {
		val dir = Files.createTempDirectory("jakon-render")
		try f(dir) finally TemplateUtils.deleteRecursively(dir)
	}

	private def assumeSymlinks(dir: Path): Unit = {
		val probe = dir.resolve("probe")
		val supported = Try(Files.createSymbolicLink(probe, dir.getFileName)).isSuccess
		Files.deleteIfExists(probe)
		assume(supported, "symlinks are not supported on this system")
	}

	private def write(dir: String, name: String, content: String): Unit = {
		Files.writeString(Path.of(dir, name), content)
	}

	private def releases(dir: Path): List[Path] = Using.resource(Files.list(dir)) { s =>
		s.iterator().asScala.filter(_.getFileName.toString.startsWith("out" + TemplateUtils.ReleaseInfix)).toList
	}

	test("renderInPlace removes stale files") {
		withTempDir(dir => {
			val out = dir.resolve("out")
			Files.createDirectories(out)
			Files.writeString(out.resolve("stale.html"), "stale")

			TemplateUtils.renderInPlace(out.toString)(target => write(target, "index.html", "new"))

			assert(!Files.exists(out.resolve("stale.html")))
			assert(Files.readString(out.resolve("index.html")) == "new")
		})
	}

	test("renderAtomically creates symlink to release") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val out = dir.resolve("out")

			TemplateUtils.renderAtomically(out.toString)(target => write(target, "index.html", "new"))

			assert(Files.isSymbolicLink(out))
			assert(!Files.readSymbolicLink(out).isAbsolute)
			assert(Files.readString(out.resolve("index.html")) == "new")
			assert(releases(dir).size == 1)
			assert(!Files.exists(dir.resolve("out.tmp"), LinkOption.NOFOLLOW_LINKS))
		})
	}

	test("renderAtomically replaces previous release") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val out = dir.resolve("out")

			TemplateUtils.renderAtomically(out.toString)(target => write(target, "old.html", "old"))
			val firstRelease = Files.readSymbolicLink(out)
			TemplateUtils.renderAtomically(out.toString)(target => write(target, "new.html", "new"))

			assert(Files.readSymbolicLink(out) != firstRelease)
			assert(!Files.exists(out.resolve("old.html")))
			assert(Files.readString(out.resolve("new.html")) == "new")
			assert(releases(dir).size == 1)
		})
	}

	test("renderAtomically redirects saveRenderedPage to release") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val out = dir.resolve("out")

			TemplateUtils.renderAtomically(out.toString)(_ => TemplateUtils.saveRenderedPage("page", "sub/page"))

			assert(Files.readString(out.resolve("sub/page.html")) == "page")
		})
	}

	test("renderAtomically migrates existing output directory") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val out = dir.resolve("out")
			Files.createDirectories(out.resolve("sub"))
			Files.writeString(out.resolve("sub/stale.html"), "stale")

			TemplateUtils.renderAtomically(out.toString)(target => write(target, "index.html", "new"))

			assert(Files.isSymbolicLink(out))
			assert(!Files.exists(out.resolve("sub")))
			assert(Files.readString(out.resolve("index.html")) == "new")
			assert(releases(dir).size == 1)
		})
	}

	test("renderAtomically keeps previous release when generate fails") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val out = dir.resolve("out")
			TemplateUtils.renderAtomically(out.toString)(target => write(target, "index.html", "old"))
			val firstRelease = Files.readSymbolicLink(out)

			assertThrows[IllegalStateException] {
				TemplateUtils.renderAtomically(out.toString)(target => {
					write(target, "index.html", "broken")
					throw new IllegalStateException("render failed")
				})
			}

			assert(Files.readSymbolicLink(out) == firstRelease)
			assert(Files.readString(out.resolve("index.html")) == "old")
			assert(releases(dir).size == 1)
			assert(!Files.exists(dir.resolve("out.tmp"), LinkOption.NOFOLLOW_LINKS))
		})
	}

	test("renderAtomically falls back to in place render when symlink can't be created") {
		withTempDir(dir => {
			val out = dir.resolve("out")
			Files.createDirectories(out)
			Files.writeString(out.resolve("stale.html"), "stale")
			// non empty directory in place of the temporary symlink can't be deleted
			Files.createDirectories(dir.resolve("out.tmp"))
			Files.writeString(dir.resolve("out.tmp/blocker"), "")

			TemplateUtils.renderAtomically(out.toString)(target => write(target, "index.html", "new"))

			assert(Files.isDirectory(out, LinkOption.NOFOLLOW_LINKS))
			assert(!Files.exists(out.resolve("stale.html")))
			assert(Files.readString(out.resolve("index.html")) == "new")
			assert(releases(dir).isEmpty)
		})
	}

	test("deleteRecursively does not follow symlinks") {
		withTempDir(dir => {
			assumeSymlinks(dir)
			val outside = dir.resolve("outside")
			Files.createDirectories(outside)
			Files.writeString(outside.resolve("keep.txt"), "keep")
			val toDelete = dir.resolve("toDelete")
			Files.createDirectories(toDelete)
			Files.createSymbolicLink(toDelete.resolve("link"), outside)

			TemplateUtils.deleteRecursively(toDelete)

			assert(!Files.exists(toDelete, LinkOption.NOFOLLOW_LINKS))
			assert(Files.exists(outside.resolve("keep.txt")))
		})
	}
}
