package com.example.user.timecircle

import android.widget.FrameLayout
import com.example.user.timecircle.common.CIRCLE_NUM
import com.example.user.timecircle.common.CommonUtil.convertToCircleIndex
import com.example.user.timecircle.common.UNIT_ANGLE
import com.example.user.timecircle.common.cocoLog
import kotlinx.coroutines.*
import org.jetbrains.anko.dimen
import kotlin.math.atan2

private const val DURATION: Long = 500
private const val SCALE1 = 1.0f
const val SCALE2 = 2.0f
private const val ZOOM_OUT_Y = 0.0f
private const val ROTATE_BASE_RIGHT_UNIT = -4
private const val ROTATE_BASE_LEFT_UNIT = 3
private const val INIT_ROTATION_ANGLE = 0.0f

class AnimationController(private val layout: FrameLayout, val animationChangeListener: AnimationChangeListener) {
    private var tempRotateAngle: Float = 0f
    private var originX = 0f
    var rotateAngle = INIT_ROTATION_ANGLE
    private var rotateBaseIndex = CIRCLE_NUM / 4
    private var rotateCoroutine: Job? = null
    private var antiClockwiseRotating = false
    private var clockwiseRotating = false
    private var isRotating = false
    var downTouchedRotatePos = Pair(0f, 0f)

    val zoomInX by lazy {
        layout.dimen(R.dimen.zoom_in_x)
    }
    val zoomInY by lazy {
        layout.dimen(R.dimen.zoom_in_y)
    }

    var isZoomed = false
        set(value) {
            if (value) zoomIn() else zoomOut()
            animationChangeListener.onZoom(value)
            field = value
        }

    init {
        initValues()
    }

    fun initValues() {
        rotateAngle = INIT_ROTATION_ANGLE
        rotateBaseIndex = CIRCLE_NUM / 4
    }

    private fun zoomIn() {
        if (!isZoomed) {
            originX = layout.x
            layout.animate().scaleX(SCALE2).scaleY(SCALE2).x(zoomInX.toFloat()).y(zoomInY.toFloat()).setDuration(DURATION).start()
        }
    }

    private fun zoomOut() {
        if (originX == 0f) return
        layout.animate().scaleX(SCALE1).scaleY(SCALE1).x(originX).y(ZOOM_OUT_Y).setDuration(DURATION).rotation(0f).start()
        initValues()
    }

    // rotateBaseIndex 와 몇 칸 떨어져 있는지 계산
    fun calculateRotateIndex(touchedIndex: Int, rotateBaseIndex: Int): Int {
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

    fun computeAndRotateState(touchedIndex: Int) {
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
        rotateBaseIndex = (index + rotateBaseIndex).convertToCircleIndex()

        rotateAngle -= UNIT_ANGLE * index

        layout.animate().rotation(rotateAngle).setDuration(50).start()
        rotateCoroutine = CoroutineScope(Dispatchers.Default).launch {
            delay(50)
            withContext(Dispatchers.Main) {
                if (isRotating) rotate(index)
                cocoLog("delay -rotate $index")
            }
        }
    }

    fun initActivityTouchValues() {
        antiClockwiseRotating = false
        clockwiseRotating = false
        isRotating = false
    }

    fun rotatingDone() {
        rotateAngle -= tempRotateAngle
        rotateBaseIndex = ((-rotateAngle / UNIT_ANGLE).toInt() % CIRCLE_NUM + CIRCLE_NUM / 4).convertToCircleIndex()
        tempRotateAngle = 0f
    }

    fun rotate(x: Float, y: Float) {
        if (isRotating) return

        val degree1 = (atan2(x, -y) * (180 / Math.PI) + 720) % 360
        val degree2 = (atan2(downTouchedRotatePos.first, -downTouchedRotatePos.second) * (180 / Math.PI) + 720) % 360
        //fixme 0-> 360 혹은 360-> 0 될 수 있어서 조정해준다. 더 좋은 방법있으면 수정바람
        val degree = (degree2 - degree1).let { if (it < -180) it + 360 else if (it > 180) it - 360 else it }.toFloat()

        isRotating = true
        layout.animate().rotation((rotateAngle - degree)).setDuration(50).start()
        tempRotateAngle = degree
        rotateCoroutine = CoroutineScope(Dispatchers.Default).launch {
            delay(50)
            isRotating = false
        }
    }

    fun rotateByFling(velocityX: Float, velocityY: Float, vectorX: Int, vectorY: Int) {

    }

    interface AnimationChangeListener {
        fun onZoom(isZoom: Boolean)
    }
}