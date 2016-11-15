package net.kibotu.base

import com.common.android.utils.ContextHelper
import com.common.android.utils.logging.Logger
import com.common.android.utils.logging.SystemLogger
import com.common.android.utils.misc.Bundler
import net.kibotu.base.ui.debugMenu.DebugMenu
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

@Config(constants = BuildConfig::class, application = MainApplication::class, sdk = intArrayOf(23))
@RunWith(RobolectricTestRunner::class)
class BundleTests {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().start().get()
        ContextHelper.with(application)
        ContextHelper.setContext(activity)
        Logger.addLogger(SystemLogger())
    }

    @Test
    fun debugBundle() {
        Assert.assertTrue(DebugMenu.isDebug(DebugMenu.createDebugArguments()))
        Assert.assertFalse(DebugMenu.isDebug(Bundler().get()))
    }
}