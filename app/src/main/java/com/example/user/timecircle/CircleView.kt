package com.example.user.timecircle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import org.jetbrains.anko.dimen

/**
 * Created by SungWoo on 2018-08-20.
 */
const val CIRCLE_NUM = 144
const val UNIT_ANGLE = -360.0f / CIRCLE_NUM
var Num = 0

class CircleView(context: Context?, private val startIndex: Int) : View(context) {
    var blueColor = Color.BLUE
    private val paint = Paint()
    private val interval = 0.2f
    private val circleStartPos = 2.0f
    private val circleSize = dimen(R.dimen.timeCircle_Length).toFloat() - circleStartPos

    init {
        paint.color = blueColor
        paint.isAntiAlias = true
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawOnCanvas(canvas)
    }

    private fun drawOnCanvas(canvas: Canvas?) {
        canvas?.drawArc(circleStartPos, circleStartPos, circleSize, circleSize, UNIT_ANGLE * startIndex - interval , UNIT_ANGLE + interval, true, paint)
    }
}