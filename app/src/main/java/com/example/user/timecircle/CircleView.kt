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
    var mColor = Color.BLUE

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = mColor
        canvas?.drawArc(0.0f, 0.0f, dimen(R.dimen.timeCircle_Length).toFloat(), dimen(R.dimen.timeCircle_Length).toFloat(), UNIT_ANGLE * startIndex, UNIT_ANGLE, true, paint)
    }
}