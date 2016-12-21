package net.kibotu.base.storage

import android.app.Application
import com.common.android.utils.extensions.LocaleExtensions.restartInLocale
import com.common.android.utils.logging.Logger
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.HawkBuilder
import com.orhanobut.hawk.LogLevel
import net.kibotu.base.BuildConfig
import net.kibotu.base.storage.LocalUser.HawkKeys.LOCALE_DEBUG
import net.kibotu.base.storage.LocalUser.HawkKeys.LOCALE_DEFAULT
import java.util.*

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */
class LocalUser {

    // region constants

    internal enum class HawkKeys {

        LOCALE_DEFAULT,
        LOCALE_DEBUG
    }

    // endregion

    companion object {

        private val TAG = LocalUser::class.java.simpleName

        @JvmStatic fun clear() {
            Hawk.clear()
        }

        @JvmStatic fun with(context: Application) {

            Hawk.init(context)
                    .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                    .setStorage(HawkBuilder.newSharedPrefStorage(context))
                    .setLogLevel(if (BuildConfig.DEBUG)
                        LogLevel.FULL
                    else
                        LogLevel.NONE)
                    .build()
        }


        // region locale

        var defaultLocale: Locale
            get() = Hawk.get(LOCALE_DEFAULT.name, Locale.ENGLISH)
            set(locale) {
                Hawk.put(LOCALE_DEFAULT.name, locale)
            }

        val debugLocale: Locale
            get() = Hawk.get(LOCALE_DEBUG.name, Locale.KOREA)

        @JvmStatic fun setDebugLocale(locale: Locale): Locale {
            Logger.v(TAG, "[setDebugLocale] " + locale)
            return Hawk.get(LOCALE_DEBUG.name, locale)
        }

        @JvmStatic fun switchLocale(locale: Locale) {
            Logger.v(TAG, "[switchToDefault] " + locale)
            LocalUser.setDebugLocale(locale)
            restartInLocale(locale)
        }

        @JvmStatic fun switchToKorean() {
            switchLocale(Locale.KOREA)
        }

        @JvmStatic fun switchToDefault() {
            switchLocale(LocalUser.defaultLocale)
        }

        // endregion

    }
}
