package jakontest.core.dynamic

import cz.kamenitxan.jakon.core.configuration.ConfigurationInitializer
import cz.kamenitxan.jakon.core.dynamic.{ConditionalPagelet, PageletSettings, PageletState}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, DoNotDiscover}

@DoNotDiscover
class PageletSettingsTest extends AnyFunSuite with BeforeAndAfterEach {

	private val configKey = "pagelet." + classOf[DummyPagelet].getSimpleName

	override protected def afterEach(): Unit = {
		ConfigurationInitializer.getConf.remove(configKey)
		super.afterEach()
	}

	test("pagelet is enabled by default") {
		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[DummyPagelet]))
		assert(PageletSettings.isEnabled(classOf[DummyPagelet]))
	}

	test("configuration disables pagelet") {
		ConfigurationInitializer.getConf.put(configKey, "DISABLED")

		assert(PageletState.DISABLED == PageletSettings.stateOf(classOf[DummyPagelet]))
		assert(!PageletSettings.isEnabled(classOf[DummyPagelet]))
	}

	test("configuration affects only given class") {
		ConfigurationInitializer.getConf.put(configKey, "DISABLED")

		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[OtherDummyPagelet]))
	}

	test("configuration value is case insensitive and trimmed") {
		ConfigurationInitializer.getConf.put(configKey, " disabled ")

		assert(PageletState.DISABLED == PageletSettings.stateOf(classOf[DummyPagelet]))
	}

	test("invalid configuration value falls back to enabled") {
		ConfigurationInitializer.getConf.put(configKey, "nonsense")

		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[DummyPagelet]))
	}

	test("pagelet can disable itself via ConditionalPagelet") {
		assert(PageletState.DISABLED == PageletSettings.stateOf(classOf[SelfDisablingPagelet]))
		assert(!PageletSettings.isEnabled(classOf[SelfDisablingPagelet]))
	}

	test("invalid configuration value falls back to ConditionalPagelet") {
		val key = "pagelet." + classOf[SelfDisablingPagelet].getSimpleName
		ConfigurationInitializer.getConf.put(key, "nonsense")
		try {
			assert(PageletState.DISABLED == PageletSettings.stateOf(classOf[SelfDisablingPagelet]))
		} finally {
			ConfigurationInitializer.getConf.remove(key)
		}
	}

	test("configuration overrides ConditionalPagelet") {
		val key = "pagelet." + classOf[SelfDisablingPagelet].getSimpleName
		ConfigurationInitializer.getConf.put(key, "ENABLED")
		try {
			assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[SelfDisablingPagelet]))
		} finally {
			ConfigurationInitializer.getConf.remove(key)
		}
	}

	test("pagelet without companion object is enabled") {
		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[DummyPagelet]))
	}

	test("companion object not implementing ConditionalPagelet is ignored") {
		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[PlainCompanionPagelet]))
	}

	test("failing ConditionalPagelet falls back to enabled") {
		assert(PageletState.ENABLED == PageletSettings.stateOf(classOf[FailingPagelet]))
	}
}

private class DummyPagelet

private class OtherDummyPagelet

private class PlainCompanionPagelet

private object PlainCompanionPagelet {
	val unrelated = 1
}

private class SelfDisablingPagelet

private object SelfDisablingPagelet extends ConditionalPagelet {
	override def defaultState: PageletState = PageletState.DISABLED
}

private class FailingPagelet

private object FailingPagelet extends ConditionalPagelet {
	override def defaultState: PageletState = throw new IllegalStateException("boom")
}
