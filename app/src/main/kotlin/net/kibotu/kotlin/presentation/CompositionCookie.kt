@file:JvmName("CompositionCookie")

package net.kibotu.kotlin.presentation

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */


fun <A, B> compose(f: (A) -> A,
                   g: (A) -> A,
                   h: (A) -> B): (A) -> B = { x -> h(g(f(x))) }