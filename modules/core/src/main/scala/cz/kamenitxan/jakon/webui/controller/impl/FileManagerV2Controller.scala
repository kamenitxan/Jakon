package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.dynamic.*
import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet

import scala.collection.mutable

/**
	* Serves the new Angular-based file manager UI (the `jakon-filemanager` project) at a
	* separate path from [[FileManagerController]], so both can be used side by side while
	* the rewrite is being tested.
	*
	* Only the template differs here - the JSON/upload/download connector API consumed by the
	* Angular frontend is unchanged and still served by [[FileManagerController]] at
	* `/admin/files/ *` (the base API path is hardcoded in the Angular app's
	* `FILE_MANAGER_API_BASE` token, independent of which page loaded it), so this controller
	* only renders the two wrapper pages and declares no `{method}` routes of its own.
	*
	* The Angular build's static assets (`main.js`, `styles.css`, `favicon.ico`, `i18n/ *.json`)
	* are checked in under `static/jakon/filemanager/`, see the `update-jakon` skill in the
	* jakon-filemanager repository for how they get there.
	*/
@Pagelet(path = "/admin/filemanager2", showInAdmin = true)
class FileManagerV2Controller extends AbstractAdminPagelet {

	override val name: String = this.getClass.getSimpleName

	override val icon: String = "fa-files-o"

	@Get(path = "", template = "objects/fileManagerV2")
	def render(): mutable.Map[String, Any] = {
		mutable.Map[String, Any]()
	}

	@Get(path = "/frame", template = "objects/fileManagerV2Frame")
	def frame(): mutable.Map[String, Any] = {
		mutable.Map[String, Any]()
	}

}

object FileManagerV2Controller extends ConditionalPagelet {
	/** Follows the same [[AdminSettings.enableFiles]] flag as the legacy file manager. */
	override def defaultState: PageletState = {
		if (AdminSettings.enableFiles) PageletState.ENABLED else PageletState.DISABLED
	}
}
