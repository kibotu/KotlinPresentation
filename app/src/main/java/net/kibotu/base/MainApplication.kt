package net.kibotu.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Build.*
import android.os.Build.VERSION.*
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.common.android.utils.ContextHelper
import com.common.android.utils.extensions.FragmentExtensions
import com.common.android.utils.extensions.JUnitExtensions.isJUnitTest
import com.common.android.utils.logging.LogcatLogger
import com.common.android.utils.logging.Logger
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.zplesac.connectionbuddy.ConnectionBuddy
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration
import io.fabric.sdk.android.Fabric
import net.danlew.android.joda.JodaTimeAndroid
import net.kibotu.android.bloodhound.BloodHound
import net.kibotu.android.deviceinfo.library.Device
import net.kibotu.base.BuildConfig.*
import net.kibotu.base.misc.ConnectivityChangeListenerRx
import net.kibotu.base.misc.DefaultUserAgent
import net.kibotu.base.storage.LocalUser
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        MultiDex.install(applicationContext)
        super.onCreate()

        JodaTimeAndroid.init(this)
        ContextHelper.with(this)
        Device.with(this)
        LocalUser.with(this)
        initLogger()

        if (isJUnitTest())
            return

        logBuildConfig()

        initFabric()

        initCalligraphy()

        initConnectivityChangeListener()

        if (resources.getBoolean(R.bool.use_stetho))
            Stetho.initializeWithDefaults(this)

        initGA()
    }

    private fun initGA() {
        if (!resources.getBoolean(R.bool.enable_google_analytics))
            return

        BloodHound.with(this, resources.getString(R.string.google_analytics_tracking_id))
                .enableExceptionReporting(false)
                .enableAdvertisingIdCollection(true)
                .enableAutoActivityTracking(false)
                .setSessionTimeout(300)
                .setSampleRate(100.0)
                .setLocalDispatchPeriod(300)
                .enableLogging(true)
                .enableDryRun(BuildConfig.DEBUG)
                .setSessionLimit(500)
    }

    private fun initCalligraphy() {
        // Default font
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                // .setDefaultFontPath(getString(R.string.amazing_font))
                .build())
    }

    private fun initLogger() {

        Logger.addLogger(LogcatLogger())
        Logger.setLogLevel(if (resources.getBoolean(R.bool.enable_logging))
            Logger.Level.VERBOSE
        else
            Logger.Level.SILENT)

        FragmentExtensions.setLoggingEnabled(resources.getBoolean(R.bool.enable_logging))
    }

    private fun initConnectivityChangeListener() {
        ConnectionBuddy.getInstance().init(ConnectionBuddyConfiguration.Builder(this).build())
        ConnectivityChangeListenerRx.with(this)
        ConnectivityChangeListenerRx.getObservable()
                .subscribe({ connectivityEvent ->
                    Logger.v(TAG, "[connectivityEvent] " + connectivityEvent)
                }, { it.printStackTrace() })
    }

    private fun initFabric() {
        Fabric.with(this, Crashlytics())
        for ((key, value) in createInfo(this))
            Crashlytics.setString(key, value)
    }

    private fun logBuildConfig() {
        for ((key, value) in createInfo(this)) {
            Logger.i(TAG, key + " : " + value)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logger.v(TAG, "[onConfigurationChanged] " + newConfig)
        LocalUser.setDefaultLocale(getLocaleFrom(newConfig))
    }

    override fun onTerminate() {
        ContextHelper.onTerminate()
        Device.onTerminate()
        ConnectivityChangeListenerRx.onTerminate(this)
        super.onTerminate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {

        private val TAG = MainApplication::class.java.simpleName

        fun createInfo(context: Context): Map<String, String> {
            val info = createAppBuildInfo(context)
            info.putAll(createDeviceBuild(context))
            return info
        }

        fun createAppBuildInfo(context: Context): MutableMap<String, String> {
            val info = LinkedHashMap<String, String>()
            info.put("DEVICE_ID", "" + Build.SERIAL)
            info.put("ANDROID ID", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
            info.put("CANONICAL_VERSION_NAME", CANONICAL_VERSION_NAME)
            info.put("SIMPLE_VERSION_NAME", SIMPLE_VERSION_NAME)
            info.put("VERSION_NAME", "" + BuildConfig.VERSION_NAME)
            info.put("VERSION_CODE", "" + VERSION_CODE)
            info.put("BUILD_TYPE", BUILD_TYPE)
            info.put("FLAVOR", FLAVOR)
            val d = Calendar.getInstance()
            d.timeInMillis = java.lang.Long.parseLong(BUILD_DATE)
            info.put("BUILD_DATE", "" + d.time)
            info.put("BRANCH", BRANCH)
            info.put("COMMIT_HASH", COMMIT_HASH)
            info.put("COMMIT_URL", VSC + "commits/" + COMMIT_HASH)
            info.put("TREE_URL", VSC + "src/" + COMMIT_HASH)
            return info
        }

        fun createDeviceBuild(context: Context): Map<String, String> {
            val info = LinkedHashMap<String, String>()
            // http://developer.android.com/reference/android/os/Build.html

            info.put("Model", MODEL)
            info.put("Manufacturer", MANUFACTURER)
            info.put("Release", Build.VERSION.RELEASE)
            info.put("SDK_INT", SDK_INT.toString())
            info.put("TIME", Date(TIME).toString())

            if (SDK_INT >= LOLLIPOP)
                info.put("SUPPORTED_ABIS", Arrays.toString(SUPPORTED_ABIS))
            else {
                info.put("CPU_ABI", CPU_ABI)
                info.put("CPU_ABI2", CPU_ABI2)
            }

            info.put("Board", BOARD)
            info.put("Bootloader", BOOTLOADER)
            info.put("Brand", BRAND)
            info.put("Device", DEVICE)
            info.put("Display", DISPLAY)
            info.put("Fingerprint", FINGERPRINT)
            info.put("Hardware", HARDWARE)
            info.put("Host", HOST)
            info.put("Id", ID)
            info.put("Product", PRODUCT)
            info.put("Serial", SERIAL)
            info.put("Tags", TAGS)
            info.put("Type", TYPE)
            info.put("User", USER)

            // http://developer.android.com/reference/android/os/Build.VERSION.html
            info.put("Codename", CODENAME)
            info.put("Incremental", INCREMENTAL)
            info.put("User Agent", DefaultUserAgent.getDefaultUserAgent(context))
            info.put("HTTP Agent", System.getProperty("http.agent"))

            return info
        }

        @SuppressLint("NewApi")
        fun getLocaleFrom(configuration: Configuration): Locale {
            return if (SDK_INT >= N)
                configuration.locales.get(0)
            else
                configuration.locale
        }
    }
}
