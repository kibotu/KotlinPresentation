@file:JvmName("TestUtils")

package net.kibotu.base

import android.net.Uri
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.gson.JsonParser
import org.junit.Assert
import java.util.*


/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

val jsonParser = JsonParser()

fun assertEqualJson(expected: String?, actual: String?) {
    Assert.assertNotNull(expected, actual)
    Assert.assertEquals(jsonParser.parse(expected), jsonParser.parse(actual))
}

fun Throwable.assertFail() {
    Assert.fail(message)
}

fun StringSubject.isNoUUID(): StringSubject {

    val actualMethod = Subject::class.java.getDeclaredMethod("actual")
    actualMethod.isAccessible = true
    val actual = actualMethod.invoke(this)

    try {
        UUID.fromString(actual.toString())

        val actualAsStringMethod = Subject::class.java.getDeclaredMethod("actualAsString")
        actualAsStringMethod.isAccessible = true
        val actualAsString = actualAsStringMethod.invoke(this)

        val failWithRawMessageMethod = Subject::class.java.getDeclaredMethod("failWithRawMessage")
        failWithRawMessageMethod.isAccessible = true
        failWithRawMessageMethod.invoke(this, "Not true that %s is UUID", actualAsString)

    } catch (e: Exception) {
    }

    return this
}

fun StringSubject.isUUID(): StringSubject {

    val actualMethod = Subject::class.java.getDeclaredMethod("actual")
    actualMethod.isAccessible = true
    val actual = actualMethod.invoke(this)

    try {
        UUID.fromString(actual.toString())
    } catch (e: Exception) {

        val actualAsStringMethod = Subject::class.java.getDeclaredMethod("actualAsString")
        actualAsStringMethod.isAccessible = true
        val actualAsString = actualAsStringMethod.invoke(this)

        val failWithRawMessageMethod = Subject::class.java.getDeclaredMethod("failWithRawMessage")
        failWithRawMessageMethod.isAccessible = true
        failWithRawMessageMethod.invoke(this, "Not true that %s is not a UUID", actualAsString)
    }

    return this
}

fun StringSubject.isValidUrl(): StringSubject {

    val actualMethod = Subject::class.java.getDeclaredMethod("actual")
    actualMethod.isAccessible = true
    val actual = actualMethod.invoke(this)

    try {
        Uri.parse(actual.toString())
    } catch (e: Exception) {

        val actualAsStringMethod = Subject::class.java.getDeclaredMethod("actualAsString")
        actualAsStringMethod.isAccessible = true
        val actualAsString = actualAsStringMethod.invoke(this)

        val failWithRawMessageMethod = Subject::class.java.getDeclaredMethod("failWithRawMessage")
        failWithRawMessageMethod.isAccessible = true
        failWithRawMessageMethod.invoke(this, "Not true that %s is not a UUID", actualAsString)
    }

    return this
}