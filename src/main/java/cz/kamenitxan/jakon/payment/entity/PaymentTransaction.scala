package cz.kamenitxan.jakon.payment.entity

import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.ManyToOne

class PaymentTransaction extends JakonObject(childClass = classOf[PaymentTransaction].getName) {
	@JakonField(disabled = true)
	var status: TransactionStatus = TransactionStatus.CREATED
	@ManyToOne
	@JakonField(disabled = true)
	var user: JakonUser = _
	@JakonField(disabled = true)
	var amount: Long = _

	override val objectSettings: ObjectSettings = null
}
