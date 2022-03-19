package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.dynamic.Pagelet

/**
 * Created by TPa on 15.03.2022.
 */
@Pagelet(path = "/admin/logs", showInAdmin = true)
class LogViewerPagelet extends AbstractAdminPagelet {
	override val name: String = this.getClass.getSimpleName


}
