package net.kibotu.base

import com.common.android.utils.misc.Bundler
import net.kibotu.base.ui.debugMenu.DebugMenu
import org.junit.Assert
import org.junit.Test

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class BundleTests : BaseTest() {

    @Test
    fun debugBundle() {
        Assert.assertTrue(DebugMenu.isDebug(DebugMenu.createDebugArguments()))
        Assert.assertFalse(DebugMenu.isDebug(Bundler().get()))
    }
}