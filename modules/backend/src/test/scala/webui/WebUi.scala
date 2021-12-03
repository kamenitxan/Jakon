package webui

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.BasicJakonObject
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.webui.api.AbstractRequest
import cz.kamenitxan.jakon.webui.functions.{AdminPebbleExtension, GetAttributeTypeFun, LinkFun}
import test.TestBase

class WebUi extends TestBase {

	test("AdminPebbleExtension") { _ =>
		val ext = new AdminPebbleExtension().getFunctions
		assert(!ext.isEmpty)
	}

	test("LinkFun") { f =>
		val fun = new LinkFun()
		val args = fun.getArgumentNames
		assert(!args.isEmpty)

		val params = new java.util.HashMap[String, AnyRef]()
		params.put("id", java.lang.Long.valueOf("1"))
		val res = fun.execute(params, null, null, 0)
		assert(res != null)
	}


	test("GetAttributeTypeFun") { _ =>
		val fun = new GetAttributeTypeFun()
		val args = fun.getArgumentNames
		assert(!args.isEmpty)

		val testObj = new BasicJakonObject
		testObj.id = 888

		val params = new java.util.HashMap[String, AnyRef]()
		params.put("attr", "id")
		params.put("object", testObj)
		val res = fun.execute(params, null, null, 0)
		assert(res != null)
		assert(res == "text")
	}

	test("AbstractRequest") { _ =>
		val req = new AbstractRequest("test")
		assert(req != null)
	}

	test("Send user password reset email") { f =>
		DBHelper.withDbConnection(implicit conn => {
			val user = UserService.getMasterAdmin
			val url = host + s"/admin/profile/object/JakonUser/${user.id}/resetPassword"
			f.driver.get(url)

			assert(checkPageLoad()(f.driver))
		})
	}
}
