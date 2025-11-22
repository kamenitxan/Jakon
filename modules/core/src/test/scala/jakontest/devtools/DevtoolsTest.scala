package jakontest.devtools

import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import jakontest.test.TestBase
import org.openqa.selenium.WebDriver
import org.scalatest.DoNotDiscover

import java.io.{BufferedWriter, File, FileWriter}

/**
 * Created by TPa on 04.04.2020.
 */
@DoNotDiscover
class DevtoolsTest extends TestBase {


	test("UploadFilesController test") { f =>
		val content = "test"
		val file = new File(FileManagerController.REPOSITORY_BASE_PATH + "/basePath/ufctest.txt")
		file.createNewFile()

		val fw = new FileWriter(file.getAbsoluteFile)
		val bw = new BufferedWriter(fw)
		bw.write(content)
		bw.close()
		fw.close()

		implicit val driver: WebDriver = f.driver
		driver.get(host + "/upload/ufctest.txt")
		assert(driver.getPageSource.contains(content))
	}



}
