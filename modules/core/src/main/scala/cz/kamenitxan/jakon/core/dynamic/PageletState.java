package cz.kamenitxan.jakon.core.dynamic;

/**
 * Determines whether a pagelet is activated during startup.
 *
 * <pre>
 * ENABLED  - routes are registered
 * DISABLED - routes are not registered at all, requests end up with 404
 * </pre>
 *
 * Whether the pagelet appears in the admin menu is decided by
 * {@link Pagelet#showInAdmin()}.
 */
public enum PageletState {
	ENABLED,
	DISABLED
}
