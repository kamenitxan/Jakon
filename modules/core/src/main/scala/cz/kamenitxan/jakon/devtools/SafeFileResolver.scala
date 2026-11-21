package cz.kamenitxan.jakon.devtools

import java.io.File
import java.nio.file.{Path, Paths}
import scala.util.Try

/**
  * Resolves request path against base directory and prevents path traversal outside of it.
  */
object SafeFileResolver {

	/**
	  * @return existing regular file located inside baseDir, None otherwise
	  */
	def resolve(baseDir: String, requestPath: String): Option[File] = {
		if (baseDir == null || requestPath == null) return None
		Try {
			val baseReal = Paths.get(baseDir).toRealPath()
			val relative = requestPath.dropWhile(c => c == '/' || c == '\\')
			val target: Path = baseReal.resolve(relative).normalize()
			if (!target.startsWith(baseReal)) {
				None
			} else {
				val targetReal = target.toRealPath()
				if (targetReal.startsWith(baseReal) && targetReal.toFile.isFile) Some(targetReal.toFile) else None
			}
		}.toOption.flatten
	}
}
