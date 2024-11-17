package jakontest.webui

import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controller.impl.Dashboard
import io.javalin.http.Context
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

/**
  * Created by TPa on 08/04/2021.
  */
@DoNotDiscover
class AdminSettingsTest extends AnyFunSuite {

	test("setDashboardController test") {
		AdminSettings.setDashboardController((ctx: Context) => Dashboard.getDashboard(ctx))
	}

}
