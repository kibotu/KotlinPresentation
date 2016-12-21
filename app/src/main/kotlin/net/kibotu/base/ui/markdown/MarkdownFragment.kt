package net.kibotu.base.ui.markdown

import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.View
import kotlinx.android.synthetic.main.fragment_markdown.*
import net.kibotu.base.R
import net.kibotu.base.ui.BaseFragment

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class MarkdownFragment : BaseFragment() {

    override val layout: Int
        get() = R.layout.fragment_markdown

    override fun onEnterStatusBarColor(): Int {
        return R.color.gray
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        if (arguments != null && !isEmpty(arguments.getString(MARKDOWN_FILENAME)))
            markdownView.loadMarkdownFromAssets(arguments.getString(MARKDOWN_FILENAME))
        else if (arguments != null && !isEmpty(arguments.getString(MARKDOWN_URL)))
            markdownView.loadUrl(arguments.getString(MARKDOWN_URL))
    }

    companion object {

        val MARKDOWN_FILENAME = "MARKDOWN_FILENAME"
        val MARKDOWN_URL = "MARKDOWN_URL"
    }
}
