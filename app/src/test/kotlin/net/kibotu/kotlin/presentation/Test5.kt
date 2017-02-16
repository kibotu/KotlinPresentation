package net.kibotu.kotlin.presentation

import com.google.common.truth.Truth.assertThat
import net.kibotu.base.BaseTest
import org.junit.Test
import kotlin.properties.Delegates

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class Test5 : BaseTest() {

    fun validate(price: Double): Boolean {
        // Validation checks
        return price < 0
    }

    @Test
    fun veto() {

        var price: Double by Delegates.vetoable(0.0) { prop, old, new ->
            validate(new)
        }

        price = 10.0
        assertThat(price).isEqualTo(10.0)

        price = -10.0
        assertThat(price).isEqualTo(10.0)
    }
}