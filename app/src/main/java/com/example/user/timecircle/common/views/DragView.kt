package com.example.user.timecircle.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.user.timecircle.R
import com.example.user.timecircle.common.cocoLog
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource

class DragView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    var moveX = 0f
    var moveY = 0f
    var originX = 0f
    var originY = 0f

    var onTouch: ((rawX: Float, rawY: Float) -> Boolean)? = null
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.action ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                originX = x
                originY = y
                moveX = x - event.rawX
                moveY = y - event.rawY
                cocoLog("x: $x   y: $y   rawX: ${event.rawX}   rawY: ${event.rawY}")
            }
            MotionEvent.ACTION_MOVE -> {
                animate().x(event.rawX + moveX).y(event.rawY + moveY).setDuration(0).start()
                cocoLog("rawX: ${event.rawX}   rawY: ${event.rawY}   moveX: $moveX   moveY: $moveY")

                backgroundColorResource = if (onTouch?.invoke(event.rawX, event.rawY) == true) {
                    R.color.transParent
                } else {
                    R.color.yellow
                }
            }
            MotionEvent.ACTION_UP -> {
                animate().x(originX).y(originY).setDuration(0).start()
            }
        }
        return true
    }
}