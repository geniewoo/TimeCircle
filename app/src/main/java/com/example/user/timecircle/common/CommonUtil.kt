package com.example.user.timecircle.common

import android.content.Context

object CommonUtil {
    fun Int.convertToCircleIndex(): Int = (this + CIRCLE_NUM) % CIRCLE_NUM

    fun square(mono: Float): Float = mono * mono

    fun square(mono: Int): Float = (mono * mono).toFloat()

    fun Float.dpToPx(context: Context): Float = this * context.resources.displayMetrics.density

    fun Float.pxToDp(context: Context): Float = this / context.resources.displayMetrics.density
}