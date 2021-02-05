package com.example.user.timecircle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.user.timecircle.common.UNIT_ANGLE
import org.jetbrains.anko.dimen

/**
 * Created by SungWoo on 2018-08-20.
 */
private const val MINUTE_INTERVAL = 0.3f
private const val HOUR_INTERVAL = 0.6f
private const val UNIT_DRAW_ANGLE = UNIT_ANGLE - (MINUTE_INTERVAL * 5 + HOUR_INTERVAL) / 6

class CircleView(context: Context?) : View(context) {
    var color = Color.BLUE
    private val circleStartPos = 2.0f
    private val circleSize = dimen(R.dimen.time_circle_length).toFloat() - circleStartPos
    private var startAngle = 0.0f
    private var initialized = false

    companion object {
        private val paint = Paint()
        private var intervalCounter = 0
        private var initStartAngle = -90.0f + HOUR_INTERVAL / 2
    }

    init {
        paint.color = color
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawOnCanvas(canvas)
    }

    private fun drawOnCanvas(canvas: Canvas?) {
        if (!initialized) {
            startAngle = initStartAngle
            initStartAngle += if (++intervalCounter % 6 == 0) {
                canvas?.drawArc(circleStartPos, circleStartPos, circleSize, circleSize, initStartAngle, UNIT_DRAW_ANGLE, true, paint)
                UNIT_DRAW_ANGLE + HOUR_INTERVAL
            } else {
                canvas?.drawArc(circleStartPos, circleStartPos, circleSize, circleSize, initStartAngle, UNIT_DRAW_ANGLE, true, paint)
                UNIT_DRAW_ANGLE + MINUTE_INTERVAL
            }
            initialized = true
        } else {
            paint.color = color
            canvas?.drawArc(circleStartPos, circleStartPos, circleSize, circleSize, startAngle, UNIT_DRAW_ANGLE, true, paint)
        }
    }

    fun changeColor(resourceId: Int) {
        color = context.getColor(resourceId)
        invalidate()
    }
}