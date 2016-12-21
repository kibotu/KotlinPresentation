package net.kibotu.base.ui.debugMenu

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.common.android.utils.ContextHelper.*
import com.common.android.utils.extensions.FragmentExtensions
import com.common.android.utils.extensions.FragmentExtensions.replaceToBackStackByFading
import com.common.android.utils.extensions.ResourceExtensions.getString
import com.common.android.utils.extensions.StringExtensions.capitalize
import com.common.android.utils.logging.Logger
import com.common.android.utils.misc.Bundler
import com.common.android.utils.misc.GsonProvider.getGsonPrettyPrinting
import com.jakewharton.processphoenix.ProcessPhoenix
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import net.kibotu.android.deviceinfo.library.ViewHelper.formatBytes
import net.kibotu.android.deviceinfo.library.memory.Ram
import net.kibotu.android.deviceinfo.library.memory.RamUsage
import net.kibotu.android.deviceinfo.library.misc.UpdateTimer
import net.kibotu.base.BuildConfig
import net.kibotu.base.MainApplication
import net.kibotu.base.R
import net.kibotu.base.storage.LocalUser
import net.kibotu.base.ui.BaseFragment
import net.kibotu.base.ui.markdown.MarkdownFragment
import net.kibotu.base.ui.markdown.RawOutputFragment
import net.kibotu.base.ui.splash.SplashScreenFragment

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class DebugMenu {
    private var drawer: Drawer? = null

    private var ramUsage: RamUsage? = null

    init {

        if (getContext()!!.resources.getBoolean(R.bool.enable_debug_menu))
            setupDebugMenu()
    }

    private fun setupDebugMenu() {

        drawer = DrawerBuilder()
                .withTranslucentStatusBar(true)
                .withActivity(getActivity()!!)
                .addDrawerItems(*createDebugMenuItems())
                .withOnDrawerItemClickListener { view, position, iDrawerItem -> this.onDrawerItemClicked(view, position, iDrawerItem) }
                .build()
    }

    private fun createDebugMenuItems(): Array<IDrawerItem<*, *>> {
        return arrayOf(

                SectionDrawerItem().withName(getString(R.string.app_name) + " - " + capitalize(BuildConfig.BUILD_TYPE.toLowerCase()) + " Menu").withIdentifier(R.string.section.toLong()).withDivider(false),

                DividerDrawerItem(),

                SecondaryDrawerItem().withIdentifier(R.string.screen_markdown_readme.toLong()).withName(BuildConfig.CANONICAL_VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"), SecondaryDrawerItem().withName(R.string.screen_markdown_changelog).withIdentifier(R.string.screen_markdown_changelog.toLong()),

                DividerDrawerItem(),

                ExpandableDrawerItem().withName("App").withIdentifier(R.string.section.toLong()).withIsExpanded(true)
                        .withSubItems(SecondaryDrawerItem().withName(R.string.debug_build_info).withIdentifier(R.string.debug_build_info.toLong()))
                        .withSubItems(SecondaryDrawerItem().withName(R.string.restart_app).withIdentifier(R.string.restart_app.toLong()))
                        .withSubItems(SecondaryDrawerItem().withName(R.string.reset_app).withIdentifier(R.string.reset_app.toLong())),

                DividerDrawerItem(),

                ExpandableDrawerItem().withName("Debug").withIdentifier(R.string.section.toLong()).withIsExpanded(false)
                        .withSubItems(SecondaryDrawerItem().withName(R.string.debug_ram).withIdentifier(R.string.debug_ram.toLong()))
                        .withSubItems(SecondaryDrawerItem().withName(R.string.debug_current_threads).withIdentifier(R.string.debug_current_threads.toLong()))
                        .withSubItems(SecondaryDrawerItem().withName(R.string.debug_current_backstack).withIdentifier(R.string.debug_current_backstack.toLong()))
                        .withSubItems(SecondaryDrawerItem().withName(R.string.debug_current_fragment).withIdentifier(R.string.debug_current_fragment.toLong())),

                DividerDrawerItem(),

                ExpandableDrawerItem().withName("Language").withIdentifier(R.string.section.toLong()).withIsExpanded(false)
                        .withSubItems(SecondaryDrawerItem().withIdentifier(R.string.language_default.toLong()).withName(R.string.language_default))
                        .withSubItems(SecondaryDrawerItem().withIdentifier(R.string.language_korean.toLong()).withName(R.string.language_korean)),


                ExpandableDrawerItem().withName("Screens").withIdentifier(R.string.section.toLong()).withIsExpanded(true)
                        .withSubItems(SecondaryDrawerItem().withIdentifier(R.string.screen_splash.toLong()).withName(R.string.screen_splash)))
    }


    private fun onDrawerItemClicked(view: View, position: Int, drawerItem: IDrawerItem<*, *>): Boolean {


        val identifier = drawerItem.identifier.toInt()
        when (identifier) {

        // app commands
            R.string.restart_app -> restartApp()

            R.string.reset_app -> {
                resetApp()
                restartApp()
            }

        // app commands
            R.string.debug_ram -> {

                if (ramUsage != null) {
                    ramUsage!!.stop()
                    ramUsage = null
                }

                ramUsage = RamUsage()
                ramUsage!!.addObserver(object : UpdateTimer.UpdateListener<Ram>() {

                    override fun update(ram: Ram) {
                        Logger.v(TAG, String.format(
                                "[Available=%s Free=%s Total=%s Used=%s]",
                                formatBytes(ram.availableInBytes),
                                formatBytes(ram.freeInBytes),
                                formatBytes(ram.totalInBytes),
                                formatBytes(ram.usedInBytes)
                        ))
                    }
                }).setUpdateInterval(10000).start()
            }

            R.string.debug_current_backstack -> FragmentExtensions.printBackStack()
            R.string.debug_current_fragment -> Logger.v(TAG, "Current Fragment " + FragmentExtensions.currentFragment())
            R.string.debug_build_info -> {
                val info = MainApplication.createInfo(getContext()!!)
                replaceToBackStackByFading<BaseFragment>(RawOutputFragment().setArgument(Bundler().putString(RawOutputFragment.RAW_OUTPUT_TEXT, getGsonPrettyPrinting().toJson(info)).get()))
            }
            R.string.debug_current_threads -> for ((key, value) in Thread.getAllStackTraces()) {
                for (element in value)
                    Logger.v(TAG, key.toString() + " -> " + element)
            }

        // language
            R.string.language_default -> LocalUser.switchToDefault()
            R.string.language_korean -> LocalUser.switchToKorean()

        // screens
            R.string.screen_markdown_readme -> replaceToBackStackByFading<BaseFragment>(MarkdownFragment().setArgument(Bundler().putString(MarkdownFragment.MARKDOWN_FILENAME, "README.md").get()))

            R.string.screen_markdown_changelog -> replaceToBackStackByFading<BaseFragment>(MarkdownFragment().setArgument(Bundler().putString(MarkdownFragment.MARKDOWN_FILENAME, "CHANGELOG.md").get()))

            R.string.screen_splash -> replaceToBackStackByFading(SplashScreenFragment())
        }

        if (identifier != R.string.section)
            drawer!!.closeDrawer()

        return false

    }

    val isDrawerOpen: Boolean
        get() = drawer != null && drawer!!.isDrawerOpen

    fun closeDrawer() {
        if (drawer != null)
            drawer!!.closeDrawer()
    }

    companion object {

        val DEBUG = "SHOW_DEBUG_LOGS"

        private val TAG = DebugMenu::class.java.simpleName

        fun createDebugArguments(): Bundle {
            return Bundler().putBoolean(DebugMenu.DEBUG, true).get()
        }

        fun isDebug(bundle: Bundle?): Boolean {
            return bundle != null && bundle.getBoolean(DebugMenu.DEBUG)
        }

        fun isDebug(fragment: Fragment): Boolean {
            return isDebug(fragment.arguments)
        }

        fun resetApp() {
            LocalUser.clear()
            val application = getApplication()
        }

        fun restartApp() {
            ProcessPhoenix.triggerRebirth(getContext()!!)
        }
    }
}
