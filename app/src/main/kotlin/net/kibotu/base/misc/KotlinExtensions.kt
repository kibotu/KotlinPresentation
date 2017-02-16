/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

@file:JvmName("KViewExtensions")

package net.kibotu.base.misc

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.common.android.utils.ContextHelper
import com.common.android.utils.extensions.FragmentExtensions
import com.common.android.utils.extensions.MathExtensions.randInt
import com.common.android.utils.extensions.MathExtensions.random
import com.common.android.utils.extensions.ResourceExtensions
import com.common.android.utils.extensions.StringExtensions
import com.common.android.utils.interfaces.LogTag
import com.common.android.utils.logging.Logger
import com.common.android.utils.misc.GsonProvider
import com.common.android.utils.ui.recyclerView.DividerItemDecoration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.kibotu.base.R
import net.kibotu.base.ui.BaseFragment
import org.parceler.Parcels
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest

fun View.setMargins(
        left: Int? = null,
        top: Int? = null,
        right: Int? = null,
        bottom: Int? = null
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams
            ?: return

    lp.setMargins(
            left ?: lp.leftMargin,
            top ?: lp.topMargin,
            right ?: lp.rightMargin,
            bottom ?: lp.bottomMargin
    )

    layoutParams = lp
}


fun View.setDimension(
        width: Int? = null,
        height: Int? = null
) {
    val params = layoutParams
    params.width = width ?: params.width
    params.height = height ?: params.height
    layoutParams = params
}

fun View?.show(isShowing: Boolean = true) {
    this?.visibility = if (isShowing) View.VISIBLE else View.INVISIBLE
}

fun View?.hide(isHiding: Boolean = true) {
    this?.visibility = if (isHiding) View.INVISIBLE else View.VISIBLE
}

fun View?.gone(isGone: Boolean = true) {
    this?.visibility = if (isGone) View.GONE else View.VISIBLE
}

fun String.capitalize() = when { length < 2 -> toUpperCase()
    else -> Character.toUpperCase(toCharArray()[0]) + substring(1).toLowerCase()
}

// region http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/util/TypedValue.java#TypedValue.applyDimension%28int%2Cfloat%2Candroid.util.DisplayMetrics%29

fun Int.dpToPx(): Int {
    return toFloat().dpToPx().toInt()
}

fun Float.dpToPx(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}

fun Int.spToPx(): Int {
    return toFloat().spToPx().toInt()
}

fun Float.spToPx(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
}

fun Int.pxToDp(): Int {
    return toFloat().pxToDp().toInt()
}

fun Float.pxToDp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Int.dpToSp(): Int {
    return toFloat().spToPx().toInt()
}

fun Float.dpToSp(): Float {
    return dpToPx() / spToPx()
}

// endregion

fun ImageView.tintIf(block: () -> Boolean, @ColorRes colorOnTrue: Int, @ColorRes colorOnFalse: Int) = tint(if (block()) colorOnTrue else colorOnFalse)

fun ImageView.tint(@ColorRes color: Int) = drawable.setColorFilter(ResourceExtensions.color(color), PorterDuff.Mode.SRC_IN)


fun View.aspect(ratio: Float = 3 / 4f) =
        post {
            val params = layoutParams
            params.height = (width / ratio).toInt()
            layoutParams = params
        }

fun View.load(url: String) {
    post {
        if (width == 0 && height == 0)
            return@post

        Glide.with(context)
                .load(url)
                .asBitmap()
                .into(object : SimpleTarget<Bitmap>(width, height) {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                        val drawable = BitmapDrawable(resources, resource)
                        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            background = drawable
                        } else {
                            @Suppress("deprecation")
                            setBackgroundDrawable(drawable)
                        }
                    }
                })
    }
}

fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(ContextCompat.getColor(ContextHelper.getContext(), color))

fun TextView.bold() = setTypeface(typeface, Typeface.DEFAULT_BOLD.style)

fun ByteArray.random(): Byte = get(random(Math.max(0, size - 1)))

fun CharArray.random(): Char = get(random(Math.max(0, size - 1)))

fun ShortArray.random(): Short = get(random(Math.max(0, size - 1)))

fun IntArray.random(): Int = get(random(Math.max(0, size - 1)))

fun LongArray.random(): Long = get(random(Math.max(0, size - 1)))

fun FloatArray.random(): Float = get(random(Math.max(0, size - 1)))

fun DoubleArray.random(): Double = get(random(Math.max(0, size - 1)))

fun BooleanArray.random(): Boolean = get(random(Math.max(0, size - 1)))

inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> String.fromJson(): T = GsonProvider.getGson().fromJson(this)

fun Context.csvFromStringRes(@StringRes id: Int): List<String> = resources.getString(id).split(",").map(String::trim).toList()

fun Int.asCsv(context: Context = ContextHelper.getContext()!!): List<String> = context.resources.getString(this).split(",").map(String::trim).toList()


fun Any.toJson(): String = GsonProvider.getGson().toJson(this)

fun Any.toJsonPrettyPrinting(): String = GsonProvider.getGsonPrettyPrinting().toJson(this)

fun String.encodeBase64(): String = StringExtensions.encodeBase64(this)

fun String.decodeBase64(): String = StringExtensions.decodeBase64(this)

fun String.sha256(charset: Charset = Charsets.UTF_8): String = "%064x".format(BigInteger(1, with(MessageDigest.getInstance("SHA-256")) {
    update(toByteArray(charset))
    digest()
}))

fun BaseFragment.replaceTransaction(@IdRes containerId: Int = R.id.fragment_container) {
    ContextHelper.getAppCompatActivity()!!
            .supportFragmentManager
            .beginTransaction()
            .replace(containerId, this, tag())
            .commit()

    FragmentExtensions.printBackStack()
}

fun BaseFragment.replaceToBackStackByFading() {
    FragmentExtensions.replaceToBackStackByFading(this)
}

fun BaseFragment.addToBackStackBySlidingHorizontally() {
    FragmentExtensions.addToBackStackBySlidingHorizontally(this)
}

fun BaseFragment.replaceByFading() {
    FragmentExtensions.replaceByFading(this)
}

fun BaseFragment.addToBackStackByFading() {
    FragmentExtensions.addToBackStackByFading(this)
}

fun BaseFragment.addTransaction(@IdRes containerId: Int = R.id.fragment_container) {
    ContextHelper.getAppCompatActivity()!!
            .supportFragmentManager
            .beginTransaction()
            .add(containerId, this, tag())
            .commit()
    FragmentExtensions.printBackStack()
}

fun View.addOnGlobalLayoutListenerOnce(onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (SDK_INT >= 16)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            else
                viewTreeObserver.removeGlobalOnLayoutListener(this)

            onGlobalLayoutListener.onGlobalLayout()
        }
    })
}

fun Int.asColor(): Int = ContextCompat.getColor(ContextHelper.getContext(), this)

fun <E> Collection<E>.random(): E = toList()[randInt(0, size - 1)]

fun <E> Collection<E>.isLast(position: Int) = position == size - 1

fun RecyclerView.addDivider(showFirstDivider: Boolean = false, showLastDivider: Boolean = false) {
    val ta = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
    addDivider(ta.getDrawable(0), showFirstDivider, showLastDivider)
    ta.recycle()
}

fun RecyclerView.addDivider(drawable: Drawable? = null, showFirstDivider: Boolean = false, showLastDivider: Boolean = false) {
    addItemDecoration(DividerItemDecoration(drawable, showFirstDivider, showLastDivider))
}

fun View.setOnTouchActionUpListener(action: (v: View, event: MotionEvent) -> Boolean) {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_UP -> {
                action(v, event)
            }
            else -> false
        }
    }
}

fun Subscription.addTo(subscriptions: CompositeSubscription) = subscriptions.add(this)


inline fun <reified T> T.wrap(): Parcelable = Parcels.wrap(this)

inline fun <reified T> Parcelable?.unwrap(): T? = Parcels.unwrap(this)

inline fun <reified T> Bundle.unwrap(key: String?): T? = getParcelable<Parcelable>(key).unwrap()

fun Int.asString(): String = Resources.getSystem().getString(this)

inline fun <reified T> Boolean.whether(yes: () -> T, no: () -> T): T = if (this) yes() else no()

inline fun <reified T> Boolean.either(t: T): Pair<Boolean, T> = Pair(this, t)

inline infix fun <reified T> Pair<Boolean, T>.or(t: T): T = if (first) second else t

fun CharSequence?.ifIsEmpty(t: CharSequence): CharSequence = if (this == null || this.isBlank()) t else this

fun String?.ifIsEmpty(t: String): String = if (this == null || this.isBlank()) t else this

fun TextView.getTrimmedText(): String = text.toString().trim { it <= ' ' }

fun TextView.getTrimmedHint(): String = hint.toString().trim { it <= ' ' }

fun Any.log(message: Any?) = Logger.v(if (this is LogTag) tag() else this.javaClass.simpleName, "$message")

inline fun <reified T> Int.times(factory: () -> T) = arrayListOf<T>().apply { for (i in 0..this@times) add(factory()) }