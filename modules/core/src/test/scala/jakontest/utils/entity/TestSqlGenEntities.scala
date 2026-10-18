package jakontest.utils.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings

/** Standalone object (no JakonObject row) stored by SqlGen - createObject and updateObject are not overridden. */
class TestStandaloneFk extends JakonObject {
	@JakonField
	var name: String = ""

	@ManyToOne
	@JakonField(required = false)
	var owner: JakonUser = _

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-cube", standAlone = true)
}

/** Object stored by SqlGen whose first field is foreign key to inherited object (TestExtUser extends JakonUser). */
class TestFirstFk extends JakonObject {
	@ManyToOne
	@JakonField(required = false)
	var ext: TestExtUser = _

	@JakonField
	var name: String = ""

	override val objectSettings: ObjectSettings = null
}
