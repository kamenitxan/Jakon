package jakontest.core

import cz.kamenitxan.jakon.core.configuration.ConfigurationInitializer
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, DoNotDiscover}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/**
 * Verifies values from jakon_secrets.properties next to the config file are merged into the configuration.
 * Uses its own key prefix and removes the keys afterwards, the configuration is global.
 */
@DoNotDiscover
class ConfigurationInitializerTest extends AnyFunSuite with BeforeAndAfterEach {

	private val prefix = "configurationInitializerTest."

	override protected def afterEach(): Unit = {
		ConfigurationInitializer.getConf.keys.filter(_.startsWith(prefix)).toSeq.foreach(ConfigurationInitializer.getConf.remove)
		super.afterEach()
	}

	test("secrets next to the config file are merged and override config values") {
		val dir = tempDir()
		val config = write(dir, "app.properties", s"${prefix}name=app\n${prefix}token=placeholder\n")
		write(dir, ConfigurationInitializer.SecretsFileName, s"${prefix}token= secret \n${prefix}password=pass\n")

		ConfigurationInitializer.init(config.toFile)

		val conf = ConfigurationInitializer.getConf
		assert(conf.get(prefix + "name").contains("app"))
		assert(conf.get(prefix + "token").contains("secret"))
		assert(conf.get(prefix + "password").contains("pass"))
	}

	test("missing secrets file is not an error") {
		val dir = tempDir()
		val config = write(dir, "app.properties", s"${prefix}name=app\n")

		ConfigurationInitializer.init(config.toFile)

		assert(ConfigurationInitializer.getConf.get(prefix + "name").contains("app"))
		assert(!ConfigurationInitializer.getConf.contains(prefix + "token"))
	}

	/** deleteOnExit runs in reverse order of registration, so the directory goes after its files. */
	private def tempDir(): Path = {
		val dir = Files.createTempDirectory("jakon-config")
		dir.toFile.deleteOnExit()
		dir
	}

	private def write(dir: Path, name: String, content: String): Path = {
		val file = dir.resolve(name)
		Files.writeString(file, content, StandardCharsets.ISO_8859_1)
		file.toFile.deleteOnExit()
		file
	}
}
