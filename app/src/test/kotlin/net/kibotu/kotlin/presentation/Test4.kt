package net.kibotu.kotlin.presentation

import net.kibotu.base.BaseTest
import org.junit.Test

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class Test4 : BaseTest() {

    fun taxed(value: Double): Double = value * 1.4
    fun discounted(value: Double): Double = value * 0.9
    fun rounded(value: Double): Double = Math.round(value).toDouble()

    val prices = listOf(21.8, 232.5, 231.3)

    @Test
    fun withoutComposition() {
        val prices = listOf(21.8, 232.5, 231.3)

//        prices.map(::taxed)
//                .map(::discounted)
//                .map(::rounded)

        val actual = prices.map { Test4::taxed }
                .map { Test4::discounted }
                .map { Test4::rounded }.toString()
    }


    @Test
    fun withComposition() {

        // val taxedDiscountedRounded = compose(::taxed, ::discounted, ::rounded)

        val taxedDiscountedRounded: (Double) -> Double = compose(
                { taxed(it) },
                { discounted(it) },
                { rounded(it) })

        val acutal = prices.map(taxedDiscountedRounded)

    }
}