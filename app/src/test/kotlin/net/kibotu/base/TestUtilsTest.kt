package net.kibotu.base

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class TestUtilsTest : BaseTest() {

    @Test
    fun isUUidTest() {
        assertThat(UUID.randomUUID().toString()).isUUID()
    }

    @Test
    fun isNoUUidTest() {
        assertThat("bla").isNoUUID()
    }
}