package jakon.payment

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.payment.PaymentService
import cz.kamenitxan.jakon.payment.entity.PaymentTransaction
import test.TestBase

class PaymentTest extends TestBase {

	var id = 0

	test("create transaction") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val t = new PaymentTransaction
			t.amount = 10
			t.user = UserService.getMasterAdmin()
			id = t.create()
			assert(id > 0)
		})
	}

	test("create payment") { _ =>
		PaymentService.createPayment(PaymentService.getTransactionById(id))
	}
}
