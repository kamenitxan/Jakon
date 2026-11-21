package jakontest.core

import cz.kamenitxan.jakon.core.configuration.{ConfigurationValue, DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.utils.JakonPebbleTemplateEngine
import cz.kamenitxan.jakon.webui.util.JakonFileLoader
import jakontest.test.{TestBase, TestEmailTypeHandler}
import org.scalatest.DoNotDiscover

import java.io.IOException
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{Inet4Address, InetSocketAddress, NetworkInterface, Socket, URI}
import scala.jdk.CollectionConverters.*

@DoNotDiscover
class SettingsTest extends TestBase {

	test("setters") { _ =>
		val loader = new JakonFileLoader(System.getProperty("user.dir") + "/" + "templates/admin", true)
		loader.setSuffix(".peb")
		Settings.setAdminEngine(new JakonPebbleTemplateEngine(loader))
		Settings.setEmailTypeHandler(new TestEmailTypeHandler)
		assertThrows[IllegalArgumentException](Settings.setStaticDir(Settings.getOutputDir))
		assertThrows[IllegalArgumentException](Settings.setOutputDir(Settings.getStaticDir))
	}

	test("getters") { _ =>
		assertNotEmpty(Settings.getDatabaseDriver)
		assert(Settings.getEmailTypeHandler != null)
		assertNotEmpty(Settings.getEmailAuth)
		assertNotEmpty(Settings.getEmailHost)
		assertNotEmpty(Settings.getEmailPort)
		assertNotEmpty(Settings.getEmailUserName)
		assertNotEmpty(Settings.getEmailPassword)
		assertNotEmpty(Settings.getEmailForceBcc)
		assertNotEmpty(Settings.getLoginPath)
		assert(Settings.getHCaptchaSiteKey == null)
		assertNotEmpty(Settings.getDeployType)
	}

	test("unknown deploy mode") { _ =>
		val current = Settings.getDeployMode
		Settings.setDeployMode("XXXXX")
		assert(DeployMode.PRODUCTION == Settings.getDeployMode)
		Settings.setDeployMode(current)
	}

	test("missing deploy mode defaults to production") { _ =>
		val field = Settings.getClass.getDeclaredField("deployMode")
		assert(field.getAnnotation(classOf[ConfigurationValue]).defaultValue() == DeployMode.PRODUCTION.toString)
	}

	test("default emailTypeHandler") { _ =>
		val h = Settings.getEmailTypeHandler
		try {
			h.handle("")
			h.afterSend("")
		} catch {
			case _: Exception => assert(false)
		}
	}

	test("host - listens on loopback only") { _ =>
		assert(Settings.getHost == "127.0.0.1")

		val loopback = new Socket()
		loopback.connect(new InetSocketAddress("127.0.0.1", Settings.getPort), 1000)
		loopback.close()

		val otherAddress = NetworkInterface.networkInterfaces().iterator().asScala
			.filter(i => i.isUp && !i.isLoopback)
			.flatMap(_.inetAddresses().iterator().asScala)
			.find(_.isInstanceOf[Inet4Address])
		otherAddress.foreach(address => {
			val socket = new Socket()
			assertThrows[IOException](socket.connect(new InetSocketAddress(address, Settings.getPort), 1000))
			socket.close()
		})
	}

	test("clacks overhead enabled by default") { _ =>
		assert(Settings.isClacksOverhead)
	}

	test("clacks overhead header present in response") { _ =>
		val client = HttpClient.newHttpClient()
		val request = HttpRequest.newBuilder(URI.create(host + "/jakon/health")).GET().build()
		val response = client.send(request, HttpResponse.BodyHandlers.discarding())
		assert(response.headers().firstValue("X-Clacks-Overhead").orElse(null) == "GNU Terry Pratchett")
	}

}
