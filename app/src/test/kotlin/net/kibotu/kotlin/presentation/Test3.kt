package net.kibotu.kotlin.presentation

import com.google.common.truth.Truth.assertThat
import net.kibotu.base.BaseTest
import org.junit.Test

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class Test3 : BaseTest() {

    @Test
    fun companionObjectName() {
        assertThat(MyCompanionObject.Jar.cookie).isNotEmpty()
        assertThat(MyCompanionObject.cookie).isNotEmpty()
    }
}