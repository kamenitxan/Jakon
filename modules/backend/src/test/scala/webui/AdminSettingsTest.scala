package webui

import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controller.impl.Dashboard
import org.scalatest.funsuite.AnyFunSuite
import spark.{Request, Response}

/**
  * Created by TPa on 08/04/2021.
  */
class AdminSettingsTest extends AnyFunSuite {

	test("setDashboardController test") {
		AdminSettings.setDashboardController((req: Request, res: Response) => Dashboard.getDashboard(req, res))
	}

}
