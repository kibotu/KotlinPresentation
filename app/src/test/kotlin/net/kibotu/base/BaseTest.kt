package net.kibotu.base

import android.app.Activity
import com.common.android.utils.ContextHelper
import com.common.android.utils.logging.Logger
import com.common.android.utils.logging.SystemLogger
import net.kibotu.base.MainActivity
import net.kibotu.base.MainApplication
import net.kibotu.base.storage.LocalUser
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

@Config(constants = BuildConfig::class, application = MainApplication::class, sdk = intArrayOf(23))
@RunWith(RobolectricTestRunner::class)
abstract class BaseTest {

    val TAG: String = javaClass.simpleName

    var activity by Delegates.notNull<Activity>()

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        activity = Robolectric.buildActivity(MainActivity::class.java).create().start().get()
        ContextHelper.with(RuntimeEnvironment.application)
        ContextHelper.setContext(activity)
        Logger.addLogger(SystemLogger())

        Logger.setLogLevel(if (activity.resources.getBoolean(R.bool.enable_logging))
            Logger.Level.VERBOSE
        else
            Logger.Level.SILENT)

        LocalUser.clear()
    }
}
