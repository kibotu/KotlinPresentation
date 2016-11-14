package net.kibotu.base;

import android.app.Activity;

import com.common.android.utils.ContextHelper;
import com.common.android.utils.logging.Logger;
import com.common.android.utils.logging.SystemLogger;
import com.common.android.utils.misc.Bundler;

import net.kibotu.base.ui.debugMenu.DebugMenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.robolectric.RuntimeEnvironment.application;

/**
 * Created by <a href="https://about.me/janrabe">Jan Rabe</a>.
 */

@Config(constants = BuildConfig.class, application = MainApplication.class, sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class BundleTests {
    @Before
    public void setUp() throws Exception {
        final Activity activity = Robolectric.buildActivity(MainActivity.class).create().start().get();
        ContextHelper.with(application);
        ContextHelper.setContext(activity);
        Logger.addLogger(new SystemLogger());
    }

    @Test
    public void debugBundle() {

        Assert.assertTrue(DebugMenu.isDebug(DebugMenu.createDebugArguments()));
        Assert.assertFalse(DebugMenu.isDebug(new Bundler().get()));
    }
}
