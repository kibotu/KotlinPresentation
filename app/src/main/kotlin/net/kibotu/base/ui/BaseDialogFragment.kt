package net.kibotu.base.ui

import android.app.Dialog
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import com.common.android.utils.ContextHelper.getFragmentActivity
import com.common.android.utils.interfaces.LayoutProvider
import com.common.android.utils.interfaces.LogTag
import com.common.android.utils.misc.UIDGenerator

/**
 * Created by jan.rabe on 26/08/16.
 * <img src="https://raw.githubusercontent.com/Aracem/android-lifecycle/master/complete_android_fragment_lifecycle.png"></img>
 */

abstract class BaseDialogFragment : DialogFragment(), LayoutProvider, LogTag {

    protected val uid = UIDGenerator.newUID()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        addFlags(window)
        return dialog
    }

    private fun addFlags(window: Window?) {
        if (window == null)
            return

        if (SDK_INT >= LOLLIPOP)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (SDK_INT >= KITKAT)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        if (SDK_INT >= LOLLIPOP_MR1) {
            window.setClipToOutline(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(layout, container, false)
        return view
    }

    fun show(): BaseDialogFragment {
        if (getFragmentActivity()!!.supportFragmentManager.findFragmentByTag(javaClass.canonicalName) != null)
            return this

        show(getFragmentActivity()!!.supportFragmentManager, javaClass.canonicalName)
        return this
    }

    override fun tag(): String {
        return javaClass.simpleName + "[" + uid + "]"
    }
}