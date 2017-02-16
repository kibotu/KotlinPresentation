package net.kibotu.kotlin.presentation

// import android.view.View // Conflict
import com.google.common.truth.Truth.assertThat
import net.kibotu.base.BaseTest
import org.junit.Test
import android.view.View as AndroidView

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class Test2 : BaseTest() {

    class View

    @Test
    fun namedView() {
        val myView: View = View()
        val androidView: AndroidView = AndroidView(activity)

        assertThat(myView).isNotInstanceOf(View::class.java)
        assertThat(androidView).isNotInstanceOf(AndroidView::class.java)
    }
}