package net.kibotu.base.ui.markdown

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.TextView
import com.common.android.utils.logging.Logger
import com.yydcdut.rxmarkdown.RxMDConfiguration
import com.yydcdut.rxmarkdown.RxMarkdown
import com.yydcdut.rxmarkdown.factory.TextFactory
import com.yydcdut.rxmarkdown.loader.DefaultLoader
import kotlinx.android.synthetic.main.fragment_raw_output.*
import net.kibotu.base.R
import net.kibotu.base.ui.BaseFragment
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class RawOutputFragment : BaseFragment() {

    override val layout: Int
        get() = R.layout.fragment_raw_output

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            val raw = bundle.getString(RAW_OUTPUT_TEXT)
            if (isEmpty(raw))
                return

            Logger.v(tag(), raw)
            setMarkdown(raw, content)
        }
    }

    companion object {

        var RAW_OUTPUT_TEXT = "RAW_OUTPUT_TEXT"

        fun setMarkdown(text: String, label: TextView) {

            val rxMDConfiguration = RxMDConfiguration.Builder(label.context).setDefaultImageSize(100, 100) //default image width & height
                    .setBlockQuotesColor(Color.LTGRAY) //default color of block quotes
                    .setHeader1RelativeSize(1.6f) //default relative size of header1
                    .setHeader2RelativeSize(1.5f) //default relative size of header2
                    .setHeader3RelativeSize(1.4f) //default relative size of header3
                    .setHeader4RelativeSize(1.3f) //default relative size of header4
                    .setHeader5RelativeSize(1.2f) //default relative size of header5
                    .setHeader6RelativeSize(1.1f) //default relative size of header6
                    .setHorizontalRulesColor(Color.LTGRAY) //default color of horizontal rules's background
                    .setInlineCodeBgColor(Color.LTGRAY) //default color of inline code's background
                    .setCodeBgColor(Color.LTGRAY) //default color of code's background
                    .setTodoColor(Color.DKGRAY) //default color of todo
                    .setTodoDoneColor(Color.DKGRAY) //default color of done
                    .setUnOrderListColor(Color.WHITE) //default color of unorder list
                    .setLinkColor(Color.RED) //default color of link text
                    .setLinkUnderline(true) //default value of whether displays link underline
                    .setRxMDImageLoader(DefaultLoader(label.context)) //default image loader
                    .setDebug(true) //default value of debug
                    .setOnLinkClickCallback { view1, link ->
                        //link click callback
                    }.build()

            RxMarkdown.with(text, label.context)
                    .config(rxMDConfiguration)
                    .factory(TextFactory.create())
                    .intoObservable()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<CharSequence>() {
                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable) {
                        }

                        override fun onNext(charSequence: CharSequence) {
                            label.setText(charSequence, TextView.BufferType.SPANNABLE)
                        }
                    })
        }
    }
}
