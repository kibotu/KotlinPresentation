package net.kibotu.base

import android.view.View

fun View?.show(isShowing: Boolean = true) {
    this?.visibility = if (isShowing) View.VISIBLE else View.INVISIBLE
}

fun String.capitalize() = when { length < 2 -> toUpperCase()
    else -> Character.toUpperCase(toCharArray()[0]) + substring(1).toLowerCase()
}