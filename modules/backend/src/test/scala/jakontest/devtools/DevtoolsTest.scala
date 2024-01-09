package jakontest.devtools

import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import org.openqa.selenium.WebDriver
import org.scalatest.DoNotDiscover
import jakontest.test.TestBase

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

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
