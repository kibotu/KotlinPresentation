package net.kibotu.base

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.view.MotionEvent
import android.view.WindowManager
import com.common.android.utils.ContextHelper.getActivity
import com.common.android.utils.extensions.DeviceExtensions.hideKeyboard
import com.common.android.utils.extensions.FragmentExtensions
import com.common.android.utils.extensions.FragmentExtensions.currentFragment
import com.common.android.utils.extensions.FragmentExtensions.replace
import com.common.android.utils.extensions.JUnitExtensions.isJUnitTest
import com.common.android.utils.extensions.KeyGuardExtensions.unlockScreen
import com.common.android.utils.extensions.SnackbarExtensions
import com.common.android.utils.interfaces.Backpress
import com.common.android.utils.interfaces.DispatchTouchEvent
import com.common.android.utils.logging.Logger
import com.common.android.utils.misc.Bundler
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.robohorse.gpversionchecker.GPVersionChecker
import com.robohorse.gpversionchecker.base.CheckingStrategy
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider.REQUEST_CHECK_SETTINGS
import net.kibotu.android.deviceinfo.library.services.SystemService.getLocationManager
import net.kibotu.android.deviceinfo.library.services.SystemService.getWifiManager
import net.kibotu.base.ui.debugMenu.DebugMenu
import net.kibotu.base.ui.splash.SplashScreenFragment
import net.kibotu.timebomb.TimeBomb
import permissions.dispatcher.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    internal var debugMenu: DebugMenu? = null
    private var newIntent: Intent? = null
    private var locationControl: SmartLocation.LocationControl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isJUnitTest())
            return

        newIntent = intent
        Logger.v(TAG, "[onCreate] savedInstanceState=$savedInstanceState intent=$newIntent")

        // Keep the screen always on
        if (resources.getBoolean(R.bool.flag_keep_screen_on))
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // unlock screen
        if (resources.getBoolean(R.bool.unlock_screen_on_start))
            unlockScreen(this)

        setContentView(R.layout.activity_main)

        debugMenu = DebugMenu()

        if (!consumeIntent())
            replace(SplashScreenFragment())

        GPVersionChecker.Builder(getActivity())
                .setCheckingStrategy(CheckingStrategy.ALWAYS)
                .showDialog(true)
                .forceUpdate(resources.getBoolean(R.bool.force_update))
                // .setCustomPackageName("net.kibotu.base")
                .setVersionInfoListener { version -> Logger.v(TAG, "version=" + version) }
                .create()
    }

    private fun consumeIntent(): Boolean {
        if (newIntent == null)
            return false

        val dataString = newIntent!!.dataString
        if (isEmpty(dataString))
            return false

        Logger.v(TAG, "[consumeIntent] " + dataString)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Bundler()
                        .putString(SplashScreenFragment::class.java.canonicalName, dataString)
                        .into(SplashScreenFragment()))
                .commitNowAllowingStateLoss()

        newIntent = null

        return true
    }

    fun startLocationTracking() {
        if (locationControl != null)
            return

        locationControl = SmartLocation.with(this).location()
        locationControl!!.start { location ->
            Logger.v(TAG, "[onLocationUpdated] location=" + location)
            locationControl!!.stop()
        }
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        TimeBomb.bombAfterDays(this, BuildConfig.BUILD_DATE, resources.getInteger(R.integer.time_bomb_delay))
    }

    override fun onStop() {
        super.onPause()
    }

    override fun onBackPressed() {

        // hide keyboard
        hideKeyboard()

        // close menu
        if (debugMenu!!.isDrawerOpen) {
            debugMenu!!.closeDrawer()
            return
        }

        // let fragments handle back press
        val fragment = currentFragment()
        if (fragment is Backpress && fragment.onBackPressed())
            return

        // pop back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            FragmentExtensions.printBackStack()
            return
        }

        // quit app
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    // region hide keyboard if raycast for specific view fails

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is DispatchTouchEvent)
            return fragment.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)

        return super.dispatchTouchEvent(ev)
    }

    // endregion

    // region location permission

    @NeedsPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    internal fun scanWifi() {
        Logger.v(TAG, "[scanWifi]")

        startLocationTracking()
        displayLocationSettingsRequest(this)
        getWifiManager().startScan()
    }

    @OnShowRationale(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    internal fun showRationaleForLocation(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(R.string.button_allow) { dialog, button -> request.proceed() }
                .setNegativeButton(R.string.button_deny) { dialog, button -> request.cancel() }
                .show()
    }

    @OnPermissionDenied(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    internal fun showDeniedForLocation() {
        SnackbarExtensions.showWarningSnack(getString(R.string.permission_location_denied))
    }

    @OnNeverAskAgain(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    internal fun showNeverAskForLocation() {
        SnackbarExtensions.showWarningSnack(getString(R.string.permission_location_neverask))
    }

    // endregion

    // region external input

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Logger.v(TAG, "[onConfigurationChanged] " + newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        Logger.v(TAG, "[onActivityResult] requestCode=$requestCode resultCode=$resultCode data=$data")

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.v(TAG, "[onNewIntent] " + intent)

        this.newIntent = intent

        consumeIntent()
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName

        // endregion

        // region public global permission trigger

        fun startWifiScanning() {
            val activity = getActivity()
            if (activity is MainActivity)
                MainActivityPermissionsDispatcher.`scanWifi$app_debugWithCheck`(activity as MainActivity?)
        }

        // endregion

        val isGPSProviderEnabled: Boolean
            get() = getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)

        fun displayLocationSettingsRequest(context: Activity) {
            if (isGPSProviderEnabled)
                return

            val googleApiClient = GoogleApiClient.Builder(context).addApi(LocationServices.API).build()
            googleApiClient.connect()

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = (10000 / 2).toLong()

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback { r ->
                val status = r.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Logger.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            // Logger.i(TAG, "PendingIntent unable to execute request.");
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }// Logger.i(TAG, "All location settings are satisfied.");
                // Logger.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
            }
        }
    }
}