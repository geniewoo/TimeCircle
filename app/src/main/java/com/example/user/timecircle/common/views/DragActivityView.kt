package com.example.user.timecircle.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.user.timecircle.ActivityComponent
import com.example.user.timecircle.R
import com.example.user.timecircle.common.cocoLog
import org.jetbrains.anko.backgroundColorResource

class DragActivityView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    var moveX = 0f
    var moveY = 0f
    var originX = 0f
    var originY = 0f
    private val activityComponent: ActivityComponent

    lateinit var onTouch: ((rawX: Float, rawY: Float, activityComponent:ActivityComponent, touchFinish: Boolean) -> Boolean)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DragActivityView).also { typedArray ->
            val colorIndex = typedArray.getInteger(R.styleable.DragActivityView_dragActivityColor, 0)
            activityComponent = ActivityComponent.values()[colorIndex]
        }.recycle()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.action ?: return false
        val windowPos = IntArray(2)
        getLocationInWindow(windowPos)
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

                backgroundColorResource = if (onTouch(event.rawX - windowPos[0] + x, event.rawY - windowPos[1] + y, activityComponent, false)) {
                    R.color.transparent
                } else {
                    activityComponent.colorRes
                }
            }
            MotionEvent.ACTION_UP -> {
                backgroundColorResource = activityComponent.colorRes
                onTouch(event.rawX - windowPos[0] + x, event.rawY - windowPos[1] + y, activityComponent, true)
                animate().x(originX).y(originY).setDuration(0).start()
            }
        }
        return true
    }
}