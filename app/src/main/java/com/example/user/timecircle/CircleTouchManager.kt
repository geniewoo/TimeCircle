package com.example.user.timecircle

import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.user.timecircle.common.DEBUG
import kotlinx.android.synthetic.main.time_circle_fragment.view.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.atan2

private const val INIT_ROTATION_ANGLE = 0.0f
private const val SCALE1 = 1.0f
private const val SCALE2 = 2.0f
private const val CIRCLE_IMAGE_SCALE1 = 1.0f
private const val CIRCLE_IMAGE_SCALE2 = 1.6f
private const val ZOOM_OUT_Y = 0.0f
private const val ZOOM_IN_Y = 500.0f
private const val ZOOM_IN_X = -540.0f
private const val DURATION: Long = 500
private const val ROTATE_BASE_RIGHT_UNIT = -4
private const val ROTATE_BASE_LEFT_UNIT = 3
private const val MAX_ROTATE_INDEX = 1
class CircleTouchManager(val layout: FrameLayout) {

    private val context = layout.context

    private var isZoomed = false
    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private var originX = 0f
    private val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    private var isSelectionMode = false
    private var rotateAngle = INIT_ROTATION_ANGLE

    private var rotateBaseIndex = CIRCLE_NUM / 4

    init {
        layout.onClick { zoomIn() }
        layout.onTouch { _, event -> onTimeCircleTouched(event) }
        layout.onLongClick { changeToSelectionMode() }

        for (i in 0 until CIRCLE_NUM) {
            val circleView = CircleView(context)
            circleView.z = 1.0f
            layout.time_circle_inner_frame_layout.addView(circleView)
            circleViews[i] = circleView
        }

        initValues()
    }

    private fun zoomIn() {
        if (!isZoomed) {
            originX = layout.x
            layout.animate().scaleX(SCALE2).scaleY(SCALE2).x(ZOOM_IN_X).y(ZOOM_IN_Y).setDuration(DURATION).start()
//            circleImageView.animate().scaleX(CIRCLE_IMAGE_SCALE2).scaleY(CIRCLE_IMAGE_SCALE2).setDuration(duration).start()
            isZoomed = true
        }
    }

    fun zoomOut() {
        if (isZoomed) {
            initValues()
            layout.animate().scaleX(SCALE1).scaleY(SCALE1).x(originX).y(ZOOM_OUT_Y).setDuration(DURATION).rotation(rotateAngle).start()
//            circleImageView.animate().scaleX(CIRCLE_IMAGE_SCALE1).scaleY(CIRCLE_IMAGE_SCALE1).setDuration(duration).start()
            isZoomed = false
        }
    }

    private fun initValues() {
        rotateAngle = INIT_ROTATION_ANGLE
        rotateBaseIndex = CIRCLE_NUM / 4
    }

    private fun changeToSelectionMode() {
        if (isZoomed) {
            isSelectionMode = true
        }
    }

    private fun onTimeCircleTouched(motionEvent: MotionEvent): Boolean {
        if (!isZoomed) {
            return false
        }

        if (motionEvent.action == MotionEvent.ACTION_UP) {
            isSelectionMode = false
            return true
        }

        if (!isSelectionMode) {
            return false
        }

        val x = centerX - motionEvent.x
        val y = centerY - motionEvent.y

        val length = square(x) + square(y)

        if (isTouchedInCircle(length)) {
            changeColorAndRotate(x, y)
        }
        return true
    }

    private fun isTouchedInCircle(length: Float): Boolean {
        return length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        // touchedIndex 현재 눌린 시간 인덱
        val touchedIndex = getCircleIndex(x, y)
        circleViews[touchedIndex]?.changeColor()

        Log.i("coco", "rotateBaseIndex $rotateBaseIndex touchedIndex $touchedIndex")
        var rotateIndex = calculateRotateIndex(touchedIndex)
        //회전 시킬 일 없으면 return
        if (rotateIndex == 0) return

        Log.i("coco", "rotateIndex $rotateIndex")
        rotateIndex = rotateIndex.coerceIn(-MAX_ROTATE_INDEX, MAX_ROTATE_INDEX)

        rotateBaseIndex += rotateIndex
        Log.i("coco", "coerceIn $rotateIndex")
        if (rotateBaseIndex > CIRCLE_NUM - 1) rotateBaseIndex -= CIRCLE_NUM
        else if (rotateBaseIndex < 0) rotateBaseIndex += CIRCLE_NUM

        cocoDebugHighlightBaseIndex()

        rotateAngle += rotateIndex * UNIT_ANGLE
        layout.animate().rotation(-rotateAngle).setDuration(100).start()
        layout.animation
    }

    private fun cocoDebugHighlightBaseIndex() {
        if (DEBUG) {
            circleViews[rotateBaseIndex]?.changeColorForDebug()
        }
    }

    // rotateBaseIndex와 몇 칸 떨어져 있는지 계산
    private fun calculateRotateIndex(touchedIndex: Int): Int {
        return when {
            touchedIndex < CIRCLE_NUM / 4 && CIRCLE_NUM * 3 / 4 < rotateBaseIndex ->
                if (CIRCLE_NUM + touchedIndex - rotateBaseIndex > ROTATE_BASE_LEFT_UNIT) {
                    CIRCLE_NUM + touchedIndex - rotateBaseIndex - ROTATE_BASE_LEFT_UNIT
                } else {
                    0
                }
            rotateBaseIndex < CIRCLE_NUM / 4 && CIRCLE_NUM * 3 / 4 < touchedIndex ->
                if (-CIRCLE_NUM + touchedIndex - rotateBaseIndex < ROTATE_BASE_RIGHT_UNIT) {
                    -CIRCLE_NUM + touchedIndex - rotateBaseIndex - ROTATE_BASE_RIGHT_UNIT
                } else {
                    0
                }
            touchedIndex - rotateBaseIndex > ROTATE_BASE_LEFT_UNIT -> touchedIndex - rotateBaseIndex - ROTATE_BASE_LEFT_UNIT
            touchedIndex - rotateBaseIndex < ROTATE_BASE_RIGHT_UNIT -> touchedIndex - rotateBaseIndex - ROTATE_BASE_RIGHT_UNIT
            else -> 0
        }
    }

    private fun getCircleIndex(x: Float, y: Float): Int {
        return (((-atan2(x, y) * (180 / Math.PI) + 360) % 360) / UNIT_ANGLE).toInt()
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()
}