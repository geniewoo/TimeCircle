package com.example.user.timecircle

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import com.example.user.timecircle.common.DEBUG
import com.example.user.timecircle.common.cocoLog
import kotlinx.android.synthetic.main.time_circle_fragment.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.atan2


private const val INIT_ROTATION_ANGLE = 0.0f
private const val SCALE1 = 1.0f
private const val SCALE2 = 2.0f
private const val CIRCLE_IMAGE_SCALE1 = 1.0f
private const val CIRCLE_IMAGE_SCALE2 = 1.6f
private const val ZOOM_OUT_Y = 0.0f
private const val DURATION: Long = 500
private const val ROTATE_BASE_RIGHT_UNIT = -4
private const val ROTATE_BASE_LEFT_UNIT = 3
private const val MAX_ROTATE_INDEX = 1

class CircleTouchManager(val layout: FrameLayout) {

    private val context = layout.context

    private var isZoomed = false
    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val ZOOM_IN_X by lazy { context.dimen(R.dimen.zoom_in_x) }
    private val ZOOM_IN_Y by lazy { context.dimen(R.dimen.zoom_in_y) }
    private var originX = 0f
    private val circleViews = arrayOfNulls<CircleView>(CIRCLE_NUM)
    private var isSelectionMode = false
    private var rotateAngle = INIT_ROTATION_ANGLE
    private var isRotating = false
    private var rotateCoroutine: Job? = null
    private var antiClockwiseRotating = false
    private var clockwiseRotating = false

    private var rotateBaseIndex = CIRCLE_NUM / 4

    init {
        layout.onClick { zoomIn() }
        layout.onTouch { _, event ->
            onTimeCircleTouched(event)
            gestureDetector.onTouchEvent(event)
        }

        for (i in 0 until CIRCLE_NUM) {
            val circleView = CircleView(context)
            circleView.z = 1.0f
            layout.time_circle_inner_frame_layout.addView(circleView)
            circleViews[i] = circleView
        }

        initValues()
    }

    private val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent?) {
            if (isZoomed) {
                isSelectionMode = true
            }
        }
    })

    private fun zoomIn() {
        if (!isZoomed) {
            originX = layout.x
            layout.animate().scaleX(SCALE2).scaleY(SCALE2).x(ZOOM_IN_X.toFloat()).y(ZOOM_IN_Y.toFloat()).setDuration(DURATION).start()
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

    private fun onTimeCircleTouched(motionEvent: MotionEvent): Boolean {
        if (!isZoomed) {
            return false
        }

        if (motionEvent.action == MotionEvent.ACTION_UP) {
            isSelectionMode = false
            isRotating = false
            antiClockwiseRotating = false
            clockwiseRotating = false
            return true
        }

        if (!isSelectionMode) {
            return false
        }

        val x = motionEvent.x - centerX
        val y = motionEvent.y - centerY


        if (isTouchedInCircle(x, y)) {
            changeColorAndRotate(x, y)
        }
        return true
    }

    private fun isTouchedInCircle(x: Float, y: Float): Boolean {
        val length = square(x) + square(y)
        return length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        // touchedIndex 현재 눌린 시간 인덱
        val touchedIndex = getCircleIndex(x, y)
        circleViews[touchedIndex]?.changeColor()

        computeRotateState(touchedIndex)

//        Log.i("coco", "rotateBaseIndex $rotateBaseIndex touchedIndex $touchedIndex")
//        var rotateIndex = calculateRotateIndex(touchedIndex)
//        //회전 시킬 일 없으면 return
//        if (rotateIndex == 0) return
//
//        Log.i("coco", "rotateIndex $rotateIndex")
//        rotateIndex = rotateIndex.coerceIn(-MAX_ROTATE_INDEX, MAX_ROTATE_INDEX)
//
//        rotateBaseIndex += rotateIndex
//        Log.i("coco", "coerceIn $rotateIndex")
//        if (rotateBaseIndex > CIRCLE_NUM - 1) rotateBaseIndex -= CIRCLE_NUM
//        else if (rotateBaseIndex < 0) rotateBaseIndex += CIRCLE_NUM
//
        cocoDebugHighlightBaseIndex()
//
//        rotateAngle += rotateIndex * UNIT_ANGLE
//        layout.animate().rotation(-rotateAngle).setDuration(100).start()
//        layout.animation
    }

    private fun computeRotateState(touchedIndex: Int) {
        // 해당 반원안에 있는지 확인
        val beforeBase = rotateBaseIndex - CIRCLE_NUM / 4
        val afterBase = rotateBaseIndex + CIRCLE_NUM / 4

        // base 기준으로 1/4 반경 안에 있는지 확인
        val adjustedTouchedIndex = if (!IntRange(beforeBase, afterBase).contains(touchedIndex)) {
            if (beforeBase > touchedIndex) touchedIndex + CIRCLE_NUM else touchedIndex - CIRCLE_NUM
        } else touchedIndex

        // 벗어나는 경우 그 방향으로 회전
        when {
            adjustedTouchedIndex < rotateBaseIndex - CIRCLE_NUM / 7 -> {
                // rotate antiClockWise
                cocoLog("-antiClock")
                if (antiClockwiseRotating) return
                cocoLog("-antiClockRotate")
                rotate(-1)
                antiClockwiseRotating = true
            }
            adjustedTouchedIndex < rotateBaseIndex - CIRCLE_NUM / 9 -> {
                // rotate antiClockWise
                cocoLog("-antiClock")
                if (antiClockwiseRotating) return
                cocoLog("-antiClockRotate")
                rotate(-1)
                antiClockwiseRotating = true
            }
            adjustedTouchedIndex > rotateBaseIndex + CIRCLE_NUM / 7 -> {
                // rotate clockWise
                cocoLog("-clock")
                if (clockwiseRotating) return
                cocoLog("-clockRotate")
                rotate(1)
                clockwiseRotating = true
            }
            adjustedTouchedIndex > rotateBaseIndex + CIRCLE_NUM / 9 -> {
                // rotate clockWise
                cocoLog("-clock")
                if (clockwiseRotating) return
                cocoLog("-clockRotate")
                rotate(1)
                clockwiseRotating = true
            }
            else -> {
                isRotating = false
                cocoLog("-else")
                antiClockwiseRotating = false
                clockwiseRotating = false
            }
        }
    }

    private fun rotate(index: Int) {
        cocoLog("-rotate $index")
        isRotating = true
        rotateBaseIndex += index

//        val rotate = RotateAnimation(rotateAngle, rotateAngle + UNIT_ANGLE * - index, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f)
//        rotate.duration = 50
//        rotate.fillAfter
//        rotate.interpolator = LinearInterpolator()
        rotateAngle -= UNIT_ANGLE * index
//        layout.startAnimation(rotate)
//        layout.animate().rotationBy(-index * UNIT_ANGLE).setDuration(50).start()

        layout.animate().rotation(rotateAngle).setDuration(50).start()
        rotateCoroutine = CoroutineScope(Dispatchers.Default).launch {
            delay(50)
            withContext(Dispatchers.Main) {
                if (isRotating) rotate(index)
                cocoLog("delay -rotate $index")
            }
        }
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

    private fun getCircleIndex(x: Float, y: Float, isOutSideRequest: Boolean = false): Int {
        var degree = atan2(x, -y) * (180 / Math.PI) + 720
        if (isOutSideRequest) {
            degree -= rotateAngle
        }
        return ((degree % 360) / UNIT_ANGLE).toInt()
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()

    fun onActivityTouch(x: Float, y: Float): Boolean {
        cocoLog("centerX : $centerX $ZOOM_IN_X x : $x    centerY : $centerY $ZOOM_IN_Y y : $y")
        // 줌상태 고려해주지 않기 때문에 2로 나누어준다.
        val activityX = (x - (ZOOM_IN_X + centerX)) / 2
        val activityY = (y - (ZOOM_IN_Y + centerY)) / 2


        val isValid = isTouchedInCircle(activityX, activityY)
        if (isValid) {
            val touchedIndex = getCircleIndex(activityX, activityY, true)
            circleViews[touchedIndex]?.activityColor()
        }
        return isValid
    }

    private enum class ROTATE {
        UP,
        DOWN,
        NONE;
    }

    fun Int.adjustIndex(): Int {
        return when {
            this >= CIRCLE_NUM -> this - CIRCLE_NUM
            this < 0 -> this + CIRCLE_NUM
            else -> this
        }
    }
}