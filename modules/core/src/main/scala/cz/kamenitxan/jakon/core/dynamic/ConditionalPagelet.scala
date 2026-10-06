package cz.kamenitxan.jakon.core.dynamic

/**
 * Lets a pagelet decide on its own whether it should be activated.
 *
 * It has to be implemented by the '''companion object''' of the pagelet, so that the decision
 * can be read without instantiating the pagelet itself.
 *
 * The returned value is only a default - it can still be overridden by the
 * `pagelet.SimpleName` configuration property.
 *
 * {{{
 * @Pagelet(path = "/admin/deploy", showInAdmin = true)
 * class DeployPagelet extends AbstractAdminPagelet {
 *   // ...
 * }
 *
 * object DeployPagelet extends ConditionalPagelet {
 *   override def defaultState: PageletState =
 *     if (Files.exists(Paths.get("servers.json"))) PageletState.ENABLED else PageletState.DISABLED
 * }
 * }}}
 */
trait ConditionalPagelet {

	/** @return the state the pagelet should have unless it is overridden by configuration */
	def defaultState: PageletState
}
