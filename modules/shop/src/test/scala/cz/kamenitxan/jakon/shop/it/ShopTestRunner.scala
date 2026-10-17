package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.shop.ShopInit
import org.scalatest.{BeforeAndAfterAll, Suites}

import java.io.File
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

/**
 * Integration test suite of the shop module. Starts the shop application against a fresh SQLite database
 * (which also seeds the default shop data) and runs all suites that need the database or running server.
 */
class ShopTestRunner extends Suites(
	new ShopEntityTest,
	new CartServiceDbTest,
	new ShopServiceDbTest,
	new ShopTaskTest,
	new ShopPagesTest,
	new CartPageletTest,
	new CartSummaryJsonPageletTest,
	new OrderStatusPageletTest,
	new OrderReturnPageletTest
) with BeforeAndAfterAll {

	private val config = "jakonConfig=jakon_config_test.properties"

	override def beforeAll(): Unit = {
		new File("target/jakonShopTest.sqlite").delete()
		new File("target/test-static").mkdirs()
		new File("target/test-out").mkdirs()
		// shop templates live in core module, Pebble does not allow "../" in template dir
		copyDir(Paths.get("../core/templates/shop"), Paths.get("target/test-templates/shop"))
		Director.init()
		Settings.setTemplateEngine(new Pebble)
		new ShopInit().run(Array[String](config))
	}

	override protected def afterAll(): Unit = {
		super.afterAll()
		JakonInit.javalin.stop()
	}

	private def copyDir(source: Path, target: Path): Unit = {
		val files = Files.walk(source)
		try {
			files.forEach(src => {
				val dest = target.resolve(source.relativize(src).toString)
				if (Files.isDirectory(src)) {
					Files.createDirectories(dest)
				} else {
					Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
				}
			})
		} finally {
			files.close()
		}
	}
}
