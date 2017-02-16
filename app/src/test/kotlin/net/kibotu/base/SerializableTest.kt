package net.kibotu.base

import com.common.android.utils.misc.GsonProvider
import com.exozet.creative.misc.toJsonPrettyPrinting
import org.junit.Assert
import org.junit.Test

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

abstract class SerializableTest(var klass: Class<*>) : BaseTest() {

    open val expectedJson: String = ""

    @Test
    fun serialize() {
        val actual = GsonProvider.getGson().fromJson(expectedJson, klass)
        Assert.assertNotNull(actual)
    }

    @Test
    fun deserialize() {
        val actual = GsonProvider.getGson().fromJson(expectedJson, klass)
        Assert.assertNotNull(actual)

        val actualJson = actual.toJsonPrettyPrinting()
        assertEqualJson(expectedJson, actualJson)
    }
}