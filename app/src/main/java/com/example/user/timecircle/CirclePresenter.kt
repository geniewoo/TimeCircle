package com.example.user.timecircle

import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.user.timecircle.common.UNIT_ANGLE
import org.jetbrains.anko.dimen
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.atan2

class CirclePresenter(private val layout: FrameLayout) {
    private val context = layout.context

    private val centerX by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private val centerY by lazy { context.dimen(R.dimen.time_circle_length) / 2 }
    private var animationController = AnimationController(layout)
    private val circleViewsController = CircleViewsController(layout)
    private var touchMode: TouchMode = TouchMode.None


    init {
        layout.onClick { animationController.zoomIn() }
        layout.onTouch { _, event ->
            onTimeCircleTouched(event)
        }

        animationController.initValues()
    }

    private fun onTimeCircleTouched(motionEvent: MotionEvent): Boolean {
        if (!animationController.isZoomed) {
            return false
        }

        val x = motionEvent.x - centerX
        val y = motionEvent.y - centerY

        when (motionEvent.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (touchMode) {
                    is TouchMode.AdjustActivity -> {
                       circleViewsController.adjustActivityDone(touchMode as TouchMode.AdjustActivity)
                    }
                }
                touchMode = TouchMode.None
                animationController.initActivityTouchValues()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                touchMode = findTouchMode(x, y)
                return touchMode != TouchMode.None
            }
            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    is TouchMode.Rotating -> {
                        changeColorAndRotate(x, y)
                    }
                    is TouchMode.AdjustActivity -> {
                        val touchedIndex = getCircleIndex(x, y)
                        (touchMode as? TouchMode.AdjustActivity)?.let {
                            circleViewsController.adjustActivity(touchedIndex, it)
                        }
                    }
                    else -> return false
                }
            }
        }
        return false
    }

    private fun findTouchMode(x: Float, y: Float): TouchMode {
        val length = square(x) + square(y)
        return if (length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)) {
            val touchedIndex = getCircleIndex(x, y)
            circleViewsController.returnIndexEdgeOfActivitySet(touchedIndex)
                    ?: TouchMode.Rotating(touchedIndex)
        } else TouchMode.None
    }

    private fun changeColorAndRotate(x: Float, y: Float) {
        // touchedIndex 현재 눌린 시간 인덱
        val touchedIndex = getCircleIndex(x, y)

        animationController.computeAndRotateState(touchedIndex)

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
//        rotateAngle += rotateIndex * UNIT_ANGLE
//        layout.animate().rotation(-rotateAngle).setDuration(100).start()
//        layout.animation
    }

    private fun getCircleIndex(x: Float, y: Float, isOutSideRequest: Boolean = false): Int {
        var degree = atan2(x, -y) * (180 / Math.PI) + 720
        if (isOutSideRequest) {
            degree -= animationController.rotateAngle
        }
        return ((degree % 360) / UNIT_ANGLE).toInt()
    }

    private fun square(mono: Float): Float = mono * mono
    private fun square(mono: Int): Float = (mono * mono).toFloat()

    fun onActivityTouch(x: Float, y: Float, touchFinish: Boolean): Boolean {
        // 줌상태 고려해주지 않기 때문에 2로 나누어준다.
        val activityX = (x - (animationController.zoomInX + centerX)) / 2
        val activityY = (y - (animationController.zoomInY + centerY)) / 2

        val isValid = findIsInCircle(activityX, activityY)
        if (isValid) {
            val touchedIndex = getCircleIndex(activityX, activityY, true)
            // 이미 다른 엑티비티가 있는 경우 return
            return circleViewsController.changeColorForActivityDrop(ActivityColor.COLOR2, touchedIndex, touchFinish)
        } else {
            circleViewsController.removeColorForActivityDrop()
        }
        return isValid
    }

    fun zoomOut() {
        if (animationController.isZoomed) {
            animationController.zoomOut()
            animationController.initValues()
        }
    }

    private fun findIsInCircle(x: Float, y: Float): Boolean {
        val length = square(x) + square(y)
        return length < square(context.dimen(R.dimen.time_circle_length) / 2) && length > square(context.dimen(R.dimen.time_image_length) / 2)
    }
}